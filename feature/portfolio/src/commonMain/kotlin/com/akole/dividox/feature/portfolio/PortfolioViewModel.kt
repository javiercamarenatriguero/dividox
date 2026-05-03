package com.akole.dividox.feature.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.feature.portfolio.PortfolioContract.PortfolioSideEffect
import com.akole.dividox.feature.portfolio.PortfolioContract.PortfolioViewEvent
import com.akole.dividox.feature.portfolio.PortfolioContract.PortfolioViewState
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PortfolioViewModel(
    private val getPortfolioWithQuotes: GetPortfolioWithQuotesUseCase,
    private val observeAppSettings: ObserveAppSettingsUseCase,
    private val currencyConverter: CurrencyConverter,
    private val connectivityManager: NetworkConnectivityManager,
) : ViewModel(),
    MVI<PortfolioViewState, PortfolioViewEvent, PortfolioSideEffect> by mvi(PortfolioViewState()) {

    private val rawHoldings = MutableStateFlow<List<SecurityHolding>>(emptyList())
    private val searchQuery = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(SortOrder())

    init {
        observeData()
        observeConnectivity()
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
                observeAppSettings(),
            ) { holdings, query, order, settings ->
                rawHoldings.value = holdings
                val targetCurrency = settings.currency
                val filtered = filterHoldings(holdings, query)
                val sorted = sortHoldings(filtered, order)
                val convertedPrices = convertHoldingPrices(holdings, targetCurrency)
                viewState.value.copy(
                    isLoading = false,
                    holdings = sorted,
                    searchQuery = query,
                    sortOrder = order,
                    currency = targetCurrency,
                    error = null,
                    convertedPrices = convertedPrices,
                )
            }.collect { newState ->
                updateViewState { newState }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            var previousConnected = true
            connectivityManager.observeConnectivity().collect { isConnected ->
                // Only refresh data on offline→online transition
                if (!previousConnected && isConnected) {
                    // Trigger data refresh by getting latest portfolio once
                    getPortfolioWithQuotes().firstOrNull()?.let { holdings ->
                        rawHoldings.value = holdings
                        val query = searchQuery.value
                        val order = sortOrder.value
                        val targetCurrency = viewState.value.currency
                        val filtered = filterHoldings(holdings, query)
                        val sorted = sortHoldings(filtered, order)
                        val convertedPrices = convertHoldingPrices(holdings, targetCurrency)
                        updateViewState {
                            copy(
                                isLoading = false,
                                holdings = sorted,
                                convertedPrices = convertedPrices,
                                error = null,
                            )
                        }
                    }
                }
                previousConnected = isConnected
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

    private suspend fun convertHoldingPrices(
        holdings: List<SecurityHolding>,
        to: Currency,
    ): Map<String, Double> = buildMap {
        holdings.forEach { holding ->
            val ticker = holding.holding.tickerId
            val price = holding.quote.price
            val from = Currency.entries.firstOrNull { it.code == holding.quote.currency } ?: Currency.USD
            val converted = currencyConverter.convert(price, from, to).getOrElse { price }
            put(ticker, converted)
        }
    }
}
