package com.akole.dividox.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.ui.resources.Currency
import com.akole.dividox.component.market.domain.usecase.SearchSecuritiesUseCase
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.usecase.AddHoldingUseCase
import com.akole.dividox.component.portfolio.domain.usecase.RemoveHoldingUseCase
import com.akole.dividox.component.portfolio.domain.usecase.UpdateHoldingUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock

class HoldingViewModel(
    private val holdingId: HoldingId?,
    private val searchSecurities: SearchSecuritiesUseCase,
    private val addHolding: AddHoldingUseCase,
    private val updateHolding: UpdateHoldingUseCase,
    private val removeHolding: RemoveHoldingUseCase,
    private val getCurrentTimeMillis: () -> Long,
) : ViewModel() {

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
        // GIVEN: initialize default currency to EUR
        _state.value = _state.value.copy(currency = Currency.EUR)
    }

    fun onEvent(event: HoldingContract.HoldingViewEvent) {
        when (event) {
            is HoldingContract.HoldingViewEvent.SearchQueryChanged -> {
                // GIVEN: user types in search field
                _state.value = _state.value.copy(searchQuery = event.query)
                performSearch(event.query)
            }

            is HoldingContract.HoldingViewEvent.SecuritySelected -> {
                // GIVEN: user selects a security from search results
                _state.value = _state.value.copy(
                    selectedSecurity = event.security,
                    searchQuery = event.security.ticker,
                    searchResults = emptyList(),
                )
            }

            is HoldingContract.HoldingViewEvent.SharesChanged -> {
                // GIVEN: user enters shares value
                _state.value = _state.value.copy(shares = event.shares)
                recalculateTotal()
            }

            is HoldingContract.HoldingViewEvent.PricePerShareChanged -> {
                // GIVEN: user enters price per share value
                _state.value = _state.value.copy(pricePerShare = event.price)
                recalculateTotal()
            }

            is HoldingContract.HoldingViewEvent.CurrencyChanged -> {
                // GIVEN: user selects a different currency
                _state.value = _state.value.copy(currency = event.currency)
            }

            HoldingContract.HoldingViewEvent.ConfirmClicked -> {
                // WHEN: user confirms (ADD or UPDATE)
                handleConfirm()
            }

            HoldingContract.HoldingViewEvent.DeleteClicked -> {
                // GIVEN: user clicks delete button (EDIT mode only)
                _state.value = _state.value.copy(showDeleteConfirmation = true)
            }

            HoldingContract.HoldingViewEvent.ConfirmDeleteClicked -> {
                // WHEN: user confirms delete in dialog
                handleDelete()
            }

            HoldingContract.HoldingViewEvent.CancelDeleteClicked -> {
                // GIVEN: user cancels delete confirmation
                _state.value = _state.value.copy(showDeleteConfirmation = false)
            }

            HoldingContract.HoldingViewEvent.DismissClicked -> {
                // User dismissed sheet (no-op here, handled by caller)
            }

            is HoldingContract.HoldingViewEvent.LoadHolding -> {
                // Not used in single ViewModel mode, but kept for compatibility
            }
        }
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            if (query.isNotBlank()) {
                try {
                    _state.value = _state.value.copy(isLoading = true)
                    val results = searchSecurities(query).getOrElse { emptyList() }
                    _state.value = _state.value.copy(
                        searchResults = results,
                        isLoading = false,
                    )
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message,
                    )
                    _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError(e.message ?: "Search failed"))
                }
            } else {
                _state.value = _state.value.copy(searchResults = emptyList())
            }
        }
    }

    private fun recalculateTotal() {
        // GIVEN: shares or pricePerShare changes, recalculate estimatedTotal
        val shares = _state.value.shares.toDoubleOrNull() ?: 0.0
        val price = _state.value.pricePerShare.toDoubleOrNull() ?: 0.0
        val total = shares * price
        _state.value = _state.value.copy(estimatedTotal = total)
    }

    private fun handleConfirm() {
        viewModelScope.launch {
            // WHEN: user confirms (ADD or UPDATE)
            if (_state.value.selectedSecurity == null) {
                _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError("Please select a security"))
                return@launch
            }
            if (_state.value.shares.isBlank()) {
                _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError("Please enter shares"))
                return@launch
            }
            if (_state.value.pricePerShare.isBlank()) {
                _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError("Please enter price per share"))
                return@launch
            }

            try {
                _state.value = _state.value.copy(isLoading = true)
                val shares = _state.value.shares.toDouble()
                val price = _state.value.pricePerShare.toDouble()
                val ticker = _state.value.selectedSecurity!!.ticker
                val currency = _state.value.currency

                if (_state.value.mode == HoldingContract.Mode.ADD) {
                    // THEN: add holding to portfolio
                    val newHolding = Holding(
                        id = HoldingId("temp"), // will be replaced by server
                        tickerId = ticker,
                        shares = shares,
                        purchasePrice = price,
                        purchaseCurrency = currency,
                        purchaseDate = getCurrentTimeMillis(),
                    )
                    val result = addHolding.execute(newHolding)
                    result.onFailure { throw it }
                } else if (_state.value.mode == HoldingContract.Mode.EDIT && _state.value.holdingId != null) {
                    // THEN: update existing holding
                    val updatedHolding = Holding(
                        id = _state.value.holdingId!!,
                        tickerId = ticker,
                        shares = shares,
                        purchasePrice = price,
                        purchaseCurrency = currency,
                        purchaseDate = _state.value.originalHolding?.purchaseDate ?: getCurrentTimeMillis(),
                    )
                    val result = updateHolding.execute(updatedHolding)
                    result.onFailure { throw it }
                }

                _state.value = _state.value.copy(isLoading = false)
                _sideEffect.send(HoldingContract.HoldingSideEffect.HapticFeedback)
                _sideEffect.send(HoldingContract.HoldingSideEffect.PositionSaved)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
                _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError(e.message ?: "Operation failed"))
            }
        }
    }

    private fun handleDelete() {
        viewModelScope.launch {
            // WHEN: user confirms delete in dialog
            if (_state.value.holdingId == null) return@launch
            try {
                _state.value = _state.value.copy(isLoading = true)
                val result = removeHolding.execute(_state.value.holdingId!!)
                result.onFailure { throw it }
                _state.value = _state.value.copy(isLoading = false)
                _sideEffect.send(HoldingContract.HoldingSideEffect.HapticFeedback)
                _sideEffect.send(HoldingContract.HoldingSideEffect.PositionDeleted)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
                _sideEffect.send(HoldingContract.HoldingSideEffect.ShowError(e.message ?: "Delete failed"))
            }
        }
    }
}
