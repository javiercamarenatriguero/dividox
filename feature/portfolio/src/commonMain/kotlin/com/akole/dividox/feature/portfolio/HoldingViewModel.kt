package com.akole.dividox.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Position form (add / edit). The security is always chosen *before* reaching this screen
 * (global search in "add" mode, security detail, or an existing holding), so the form never
 * contains its own search.
 *
 * ADD mode pre-fills price (current quote), currency (quote currency) and date (today) so adding
 * a position only requires typing the number of shares.
 */
@Suppress("TooManyFunctions")
class HoldingViewModel(
    private val holdingId: HoldingId?,
    private val prefillTicker: String? = null,
    private val getStockQuote: GetStockQuoteUseCase,
    private val positionActions: PositionActions,
    private val getPortfolio: GetPortfolioUseCase,
    private val getCurrentTimeMillis: () -> Long,
    private val observeAppSettings: ObserveAppSettingsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        HoldingContract.HoldingViewState(
            mode = if (holdingId != null) HoldingContract.Mode.EDIT else HoldingContract.Mode.ADD,
            holdingId = holdingId,
            purchaseDateMillis = getCurrentTimeMillis(),
            isLoadingSecurity = holdingId != null || prefillTicker != null,
        ),
    )
    val viewState: StateFlow<HoldingContract.HoldingViewState> = _state.asStateFlow()

    private val _sideEffect = Channel<HoldingContract.HoldingSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val settings = observeAppSettings().first()
            // Only a fallback: a resolved security/holding dictates its own currency.
            _state.update { if (it.selectedSecurity == null) it.copy(currency = settings.currency) else it }
        }
        if (holdingId != null) {
            loadExistingHolding(holdingId)
        } else if (prefillTicker != null) {
            prefillSecurity(prefillTicker)
        }
    }

    fun onEvent(event: HoldingContract.HoldingViewEvent) {
        when (event) {
            is HoldingContract.HoldingViewEvent.SharesChanged -> {
                // Whole shares only: drop anything from the first non-digit on ("10.5" -> "10").
                _state.update { it.copy(shares = event.shares.takeWhile { char -> char.isDigit() }) }
                recalculateTotal()
            }

            is HoldingContract.HoldingViewEvent.PricePerShareChanged -> {
                _state.update { it.copy(pricePerShare = event.price) }
                recalculateTotal()
            }

            is HoldingContract.HoldingViewEvent.CurrencyChanged -> _state.update { it.copy(currency = event.currency) }

            is HoldingContract.HoldingViewEvent.PurchaseDateChanged ->
                _state.update { it.copy(purchaseDateMillis = event.dateMillis) }

            HoldingContract.HoldingViewEvent.ConfirmClicked -> handleConfirm()

            HoldingContract.HoldingViewEvent.DeleteClicked -> _state.update { it.copy(showDeleteConfirmation = true) }

            HoldingContract.HoldingViewEvent.ConfirmDeleteClicked -> handleDelete()

            HoldingContract.HoldingViewEvent.CancelDeleteClicked ->
                _state.update { it.copy(showDeleteConfirmation = false) }

            HoldingContract.HoldingViewEvent.DismissClicked -> Unit
            is HoldingContract.HoldingViewEvent.LoadHolding -> Unit
        }
    }

    /**
     * Loads existing holding data when entering EDIT mode (e.g., tapping a holding in Portfolio).
     * Fetches the portfolio to find the matching holding, then fetches the live StockQuote
     * so the SelectedSecurityCard can show up-to-date price/change data.
     */
    private fun loadExistingHolding(id: HoldingId) {
        viewModelScope.launch {
            val holdings = getPortfolio.execute().first().getOrNull()
            val holding = holdings?.firstOrNull { it.id == id }
            if (holding == null) {
                _state.update { it.copy(isLoadingSecurity = false) }
                return@launch
            }

            val quote = getStockQuote(holding.tickerId).getOrNull() ?: StockQuote(
                ticker = holding.tickerId,
                price = holding.purchasePrice,
                change = 0.0,
                changePercent = 0.0,
                currency = holding.purchaseCurrency.code,
                lastUpdated = Clock.System.now(),
            )

            _state.update { it.copy(selectedSecurity = quote, isLoadingSecurity = false).withHolding(holding) }
            recalculateTotal()
        }
    }

    private fun prefillSecurity(ticker: String) {
        viewModelScope.launch {
            val quote = getStockQuote(ticker).getOrNull()
            if (quote == null) {
                _state.update { it.copy(isLoadingSecurity = false) }
                return@launch
            }
            _state.update {
                it.copy(
                    selectedSecurity = quote,
                    isLoadingSecurity = false,
                    pricePerShare = quote.price.toPriceInput(),
                    currency = Currency.entries.firstOrNull { c -> c.code == quote.currency } ?: it.currency,
                )
            }
            recalculateTotal()
            checkPortfolioForExistingHolding(ticker)
        }
    }

    /**
     * If the chosen ticker already exists in the portfolio, switch to EDIT mode pre-filled with the
     * existing holding so the user updates rather than duplicates a position.
     */
    private suspend fun checkPortfolioForExistingHolding(ticker: String) {
        val holdings = getPortfolio.execute().first().getOrNull() ?: return
        val existing = holdings.firstOrNull { it.tickerId == ticker } ?: return
        _state.update { it.withHolding(existing) }
        recalculateTotal()
    }

    private fun HoldingContract.HoldingViewState.withHolding(holding: Holding) = copy(
        mode = HoldingContract.Mode.EDIT,
        holdingId = holding.id,
        originalHolding = holding,
        shares = holding.shares.toSharesInput(),
        pricePerShare = holding.purchasePrice.toPriceInput(),
        currency = holding.purchaseCurrency,
        purchaseDateMillis = holding.purchaseDate,
    )

    private fun recalculateTotal() {
        _state.update {
            val shares = it.shares.toDoubleOrNull() ?: 0.0
            val price = it.pricePerShare.toDoubleOrNull() ?: 0.0
            it.copy(estimatedTotal = shares * price)
        }
    }

    @Suppress("ReturnCount")
    private fun handleConfirm() {
        val state = _state.value
        val security = state.selectedSecurity ?: return sendError("Please select a security")
        val shares = state.shares.toLongOrNull()?.toDouble() ?: return sendError("Please enter shares")
        val price = state.pricePerShare.toDoubleOrNull() ?: return sendError("Please enter price per share")

        val holding = Holding(
            id = state.holdingId ?: HoldingId("temp"),
            tickerId = security.ticker,
            shares = shares,
            purchasePrice = price,
            purchaseCurrency = state.currency,
            purchaseDate = state.purchaseDateMillis,
        )
        if (state.mode == HoldingContract.Mode.EDIT && state.holdingId != null) {
            positionActions.update(holding)
        } else {
            positionActions.add(holding)
        }
        // Navigate back immediately (optimistic); the write and its confirmation live in PositionActions.
        _state.update { it.copy(operationCompleted = true, operationIsDelete = false) }
        viewModelScope.launch { _sideEffect.send(HoldingContract.HoldingSideEffect.PositionSaved) }
    }

    private fun handleDelete() {
        val state = _state.value
        val holdingId = state.holdingId ?: return
        positionActions.remove(holdingId, state.selectedSecurity?.ticker ?: state.originalHolding?.tickerId.orEmpty())
        _state.update { it.copy(operationCompleted = true, operationIsDelete = true, showDeleteConfirmation = false) }
        viewModelScope.launch { _sideEffect.send(HoldingContract.HoldingSideEffect.PositionDeleted) }
    }

    private fun sendError(message: String) {
        viewModelScope.launch { _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError(message)) }
    }
}

private const val PRICE_DECIMALS = 2
private const val SMALL_PRICE_DECIMALS = 4
private const val DECIMAL_BASE = 10.0

/** Share count as an editable form value: whole number only, so backspacing never hits a "." */
internal fun Double.toSharesInput(): String = roundToLong().toString()

/** Quote price as an editable form value: 2 decimals (4 for sub-unit prices), no trailing ".0". */
internal fun Double.toPriceInput(): String {
    val factor = DECIMAL_BASE.pow(if (this >= 1.0) PRICE_DECIMALS else SMALL_PRICE_DECIMALS)
    val text = (round(this * factor) / factor).toString()
    return text.removeSuffix(".0")
}
