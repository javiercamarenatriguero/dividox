package com.akole.dividox.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.ui.resources.components.ExchangeMarket
import com.akole.dividox.component.market.domain.model.SecurityType
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.market.domain.usecase.SearchSecuritiesUseCase
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.usecase.AddHoldingUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.component.portfolio.domain.usecase.RemoveHoldingUseCase
import com.akole.dividox.component.portfolio.domain.usecase.UpdateHoldingUseCase
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HoldingViewModel(
    private val holdingId: HoldingId?,
    private val prefillTicker: String? = null,
    private val searchSecurities: SearchSecuritiesUseCase,
    private val getStockQuote: GetStockQuoteUseCase,
    private val addHolding: AddHoldingUseCase,
    private val updateHolding: UpdateHoldingUseCase,
    private val removeHolding: RemoveHoldingUseCase,
    private val getPortfolio: GetPortfolioUseCase,
    private val getCurrentTimeMillis: () -> Long,
    private val observeAppSettings: ObserveAppSettingsUseCase,
) : ViewModel() {

    private var allSearchResults: List<StockQuote> = emptyList()

    private val _state = MutableStateFlow(
        HoldingContract.HoldingViewState(
            mode = if (holdingId != null) HoldingContract.Mode.EDIT else HoldingContract.Mode.ADD,
            holdingId = holdingId,
        )
    )
    val viewState: StateFlow<HoldingContract.HoldingViewState> = _state.asStateFlow()

    private val _sideEffect = Channel<HoldingContract.HoldingSideEffect>(Channel.BUFFERED)
    val sideEffect = _sideEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            val settings = observeAppSettings().first()
            val defaultMarket = ExchangeMarket.entries.firstOrNull { it.name == settings.defaultMarket }
                ?: ExchangeMarket.ALL
            _state.value = _state.value.copy(currency = settings.currency, selectedMarket = defaultMarket)
        }
        if (holdingId != null) {
            loadExistingHolding(holdingId)
        } else if (prefillTicker != null) {
            prefillSecurity(prefillTicker)
        }
    }

    fun onEvent(event: HoldingContract.HoldingViewEvent) {
        when (event) {
            is HoldingContract.HoldingViewEvent.SearchQueryChanged -> {
                _state.value = _state.value.copy(searchQuery = event.query)
                performSearch(event.query)
            }

            is HoldingContract.HoldingViewEvent.MarketFilterChanged -> {
                _state.value = _state.value.copy(
                    selectedMarket = event.market,
                    searchResults = allSearchResults.filterByMarket(event.market).filterByType(_state.value.selectedType),
                )
            }

            is HoldingContract.HoldingViewEvent.TypeFilterChanged -> {
                _state.value = _state.value.copy(
                    selectedType = event.type,
                    searchResults = allSearchResults.filterByMarket(_state.value.selectedMarket).filterByType(event.type),
                )
            }

            is HoldingContract.HoldingViewEvent.SecuritySelected -> {
                _state.value = _state.value.copy(
                    selectedSecurity = event.security,
                    searchQuery = event.security.ticker,
                    searchResults = emptyList(),
                )
                checkPortfolioForExistingHolding(event.security.ticker)
            }

            is HoldingContract.HoldingViewEvent.SharesChanged -> {
                _state.value = _state.value.copy(shares = event.shares)
                recalculateTotal()
            }

            is HoldingContract.HoldingViewEvent.PricePerShareChanged -> {
                _state.value = _state.value.copy(pricePerShare = event.price)
                recalculateTotal()
            }

            is HoldingContract.HoldingViewEvent.CurrencyChanged -> {
                _state.value = _state.value.copy(currency = event.currency)
            }

            is HoldingContract.HoldingViewEvent.PurchaseDateChanged -> {
                _state.value = _state.value.copy(purchaseDateMillis = event.dateMillis)
            }

            HoldingContract.HoldingViewEvent.ConfirmClicked -> handleConfirm()

            HoldingContract.HoldingViewEvent.DeleteClicked -> {
                _state.value = _state.value.copy(showDeleteConfirmation = true)
            }

            HoldingContract.HoldingViewEvent.ConfirmDeleteClicked -> handleDelete()

            HoldingContract.HoldingViewEvent.CancelDeleteClicked -> {
                _state.value = _state.value.copy(showDeleteConfirmation = false)
            }

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
            val holdings = getPortfolio.execute().first().getOrNull() ?: return@launch
            val holding = holdings.firstOrNull { it.id == id } ?: return@launch

            val quote = getStockQuote(holding.tickerId).getOrNull() ?: StockQuote(
                ticker = holding.tickerId,
                price = holding.purchasePrice,
                change = 0.0,
                changePercent = 0.0,
                currency = holding.purchaseCurrency.code,
                lastUpdated = Clock.System.now(),
            )

            _state.value = _state.value.copy(
                originalHolding = holding,
                holdingId = holding.id,
                selectedSecurity = quote,
                searchQuery = holding.tickerId,
                shares = holding.shares.toString(),
                pricePerShare = holding.purchasePrice.toString(),
                currency = holding.purchaseCurrency,
                purchaseDateMillis = holding.purchaseDate,
            )
            recalculateTotal()
        }
    }

    private fun prefillSecurity(ticker: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(searchQuery = ticker, isSearching = true)
            val quote = getStockQuote(ticker).getOrNull() ?: run {
                _state.value = _state.value.copy(isSearching = false)
                return@launch
            }
            _state.value = _state.value.copy(
                selectedSecurity = quote,
                searchQuery = ticker,
                searchResults = emptyList(),
                isSearching = false,
            )
            checkPortfolioForExistingHolding(ticker)
        }
    }

    /**
     * When the user selects a security in ADD mode, checks whether that ticker already exists
     * in the portfolio. If it does, the form is switched to EDIT mode and pre-filled with the
     * existing holding's data so the user updates rather than duplicates a position.
     */
    private fun checkPortfolioForExistingHolding(ticker: String) {
        if (_state.value.mode == HoldingContract.Mode.EDIT) return
        viewModelScope.launch {
            val holdings = getPortfolio.execute().first().getOrNull() ?: return@launch
            val existing = holdings.firstOrNull { it.tickerId == ticker } ?: return@launch
            _state.value = _state.value.copy(
                mode = HoldingContract.Mode.EDIT,
                holdingId = existing.id,
                originalHolding = existing,
                shares = existing.shares.toString(),
                pricePerShare = existing.purchasePrice.toString(),
                currency = existing.purchaseCurrency,
                purchaseDateMillis = existing.purchaseDate,
            )
            recalculateTotal()
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            if (query.isNotBlank()) {
                try {
                    val market = _state.value.selectedMarket
                    _state.value = _state.value.copy(isSearching = true)
                    val results = searchSecurities(query, market.region).getOrElse { emptyList() }
                    allSearchResults = results
                    _state.value = _state.value.copy(
                        searchResults = results.filterByMarket(market).filterByType(_state.value.selectedType),
                        isSearching = false,
                    )
                } catch (e: Exception) {
                    _state.value = _state.value.copy(isSearching = false, error = e.message)
                    _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError(e.message ?: "Search failed"))
                }
            } else {
                allSearchResults = emptyList()
                _state.value = _state.value.copy(searchResults = emptyList())
            }
        }
    }

    private fun List<StockQuote>.filterByMarket(market: ExchangeMarket): List<StockQuote> =
        if (market == ExchangeMarket.ALL) this else filter { market.matches(it.exchange) }

    private fun List<StockQuote>.filterByType(type: SecurityType?): List<StockQuote> =
        if (type == null) this else filter { it.type == type }

    private fun recalculateTotal() {
        val shares = _state.value.shares.toDoubleOrNull() ?: 0.0
        val price = _state.value.pricePerShare.toDoubleOrNull() ?: 0.0
        _state.value = _state.value.copy(estimatedTotal = shares * price)
    }

    private fun handleConfirm() {
        val state = _state.value
        if (state.selectedSecurity == null) {
            viewModelScope.launch {
                _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError("Please select a security"))
            }
            return
        }
        if (state.shares.isBlank()) {
            viewModelScope.launch {
                _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError("Please enter shares"))
            }
            return
        }
        if (state.pricePerShare.isBlank()) {
            viewModelScope.launch {
                _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError("Please enter price per share"))
            }
            return
        }

        val shares = state.shares.toDouble()
        val price = state.pricePerShare.toDouble()
        val ticker = state.selectedSecurity.ticker
        val currency = state.currency

        // Navigate back immediately (optimistic), then execute in background
        viewModelScope.launch {
            _state.value = _state.value.copy(operationCompleted = true, operationIsDelete = false)
            _sideEffect.send(HoldingContract.HoldingSideEffect.PositionSaved)
        }

        viewModelScope.launch {
            if (state.mode == HoldingContract.Mode.ADD) {
                addHolding.execute(
                    Holding(
                        id = HoldingId("temp"),
                        tickerId = ticker,
                        shares = shares,
                        purchasePrice = price,
                        purchaseCurrency = currency,
                        purchaseDate = state.purchaseDateMillis,
                    )
                )
            } else if (state.mode == HoldingContract.Mode.EDIT && state.holdingId != null) {
                updateHolding.execute(
                    Holding(
                        id = state.holdingId,
                        tickerId = ticker,
                        shares = shares,
                        purchasePrice = price,
                        purchaseCurrency = currency,
                        purchaseDate = state.purchaseDateMillis,
                    )
                )
            }
        }
    }
    private fun handleDelete() {
        val holdingId = _state.value.holdingId ?: return

        // Navigate back immediately (optimistic), then execute in background
        viewModelScope.launch {
            _state.value = _state.value.copy(operationCompleted = true, operationIsDelete = true)
            _sideEffect.send(HoldingContract.HoldingSideEffect.PositionDeleted)
        }

        viewModelScope.launch {
            removeHolding.execute(holdingId)
        }
    }
}
