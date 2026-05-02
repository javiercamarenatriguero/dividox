package com.akole.dividox.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.feature.portfolio.PortfolioContract.PortfolioSideEffect
import com.akole.dividox.feature.portfolio.PortfolioContract.PortfolioViewEvent
import com.akole.dividox.feature.portfolio.PortfolioContract.PortfolioViewState
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class PortfolioViewModel(
    private val getPortfolioWithQuotes: GetPortfolioWithQuotesUseCase,
    private val observeAppSettings: ObserveAppSettingsUseCase,
) : ViewModel(),
    MVI<PortfolioViewState, PortfolioViewEvent, PortfolioSideEffect> by mvi(PortfolioViewState()) {

    private val rawHoldings = MutableStateFlow<List<SecurityHolding>>(emptyList())
    private val searchQuery = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(SortOrder())

    init {
        observeData()
        observeSettings()
    }

    override fun onViewEvent(viewEvent: PortfolioViewEvent) {
        when (viewEvent) {
            is PortfolioViewEvent.SearchQueryChanged -> {
                searchQuery.value = viewEvent.query
                updateViewState { copy(searchQuery = viewEvent.query) }
            }
            is PortfolioViewEvent.SortOrderChanged -> {
                sortOrder.value = viewEvent.order
                updateViewState { copy(sortOrder = viewEvent.order) }
            }
            PortfolioViewEvent.AddHoldingClicked -> {
                viewModelScope.emitSideEffect(
                    PortfolioSideEffect.Navigation.NavigateToAddHolding
                )
            }
            is PortfolioViewEvent.EditHoldingClicked -> {
                viewModelScope.emitSideEffect(
                    PortfolioSideEffect.Navigation.NavigateToEditHolding(viewEvent.holdingId)
                )
            }
            is PortfolioViewEvent.SecurityClicked -> {
                viewModelScope.emitSideEffect(
                    PortfolioSideEffect.Navigation.NavigateToSecurity(viewEvent.ticker)
                )
            }
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                getPortfolioWithQuotes(),
                searchQuery,
                sortOrder,
            ) { holdings, query, order ->
                rawHoldings.value = holdings
                val filtered = filterHoldings(holdings, query)
                val sorted = sortHoldings(filtered, order)
                viewState.value.copy(
                    isLoading = false,
                    holdings = sorted,
                    searchQuery = query,
                    sortOrder = order,
                    error = null,
                )
            }.collect { newState ->
                updateViewState { newState }
            }
        }
    }

    private fun filterHoldings(holdings: List<SecurityHolding>, query: String): List<SecurityHolding> {
        if (query.isBlank()) return holdings
        val lowerQuery = query.lowercase()
        return holdings.filter { holding ->
            holding.holding.tickerId.lowercase().contains(lowerQuery)
        }
    }

    private fun sortHoldings(
        holdings: List<SecurityHolding>,
        sortOrder: SortOrder,
    ): List<SecurityHolding> {
        val comparator: Comparator<SecurityHolding> = when (sortOrder.field) {
            SortField.GAIN -> compareBy { it.totalGainPercent }
            SortField.YIELD -> compareBy { it.dividendInfo?.yield ?: 0.0 }
            SortField.DATE -> compareBy { it.holding.purchaseDate }
        }
        return if (sortOrder.ascending) {
            holdings.sortedWith(comparator)
        } else {
            holdings.sortedWith(comparator.reversed())
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            observeAppSettings().collect { settings ->
                updateViewState { copy(currency = settings.currency) }
            }
        }
    }
}
