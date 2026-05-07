package com.akole.dividox.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.market.domain.usecase.SearchSecuritiesUseCase
import com.akole.dividox.component.watchlist.domain.usecase.AddToWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.GetWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.search.SearchContract.SearchSideEffect
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.BackClicked
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.FavouriteToggled
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.MarketFilterChanged
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.QueryChanged
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.SecurityClicked
import com.akole.dividox.feature.search.SearchContract.SearchViewState
import com.akole.dividox.component.market.domain.model.SecurityType
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.TypeFilterChanged
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchSecurities: SearchSecuritiesUseCase,
    private val getWatchlist: GetWatchlistUseCase,
    private val addToWatchlist: AddToWatchlistUseCase,
    private val removeFromWatchlist: RemoveFromWatchlistUseCase,
    private val connectivityManager: NetworkConnectivityManager,
    private val observeAppSettings: ObserveAppSettingsUseCase,
) : ViewModel(),
    MVI<SearchViewState, SearchViewEvent, SearchSideEffect> by mvi(SearchViewState()) {

    private val queryFlow = MutableStateFlow("")
    private var allResults: List<StockQuote> = emptyList()

    init {
        initDefaultMarket()
        observeSearch()
        observeWatchlist()
        observeConnectivity()
    }

    private fun initDefaultMarket() {
        viewModelScope.launch {
            val settings = observeAppSettings().first()
            val market = ExchangeMarket.entries.firstOrNull { it.name == settings.defaultMarket }
                ?: ExchangeMarket.ALL
            updateViewState { copy(selectedMarket = market) }
        }
    }

    override fun onViewEvent(viewEvent: SearchViewEvent) {
        when (viewEvent) {
            is QueryChanged -> {
                updateViewState { copy(query = viewEvent.query) }
                queryFlow.value = viewEvent.query
            }
            is MarketFilterChanged -> {
                updateViewState {
                    copy(
                        selectedMarket = viewEvent.market,
                        results = allResults.filterByMarket(viewEvent.market).filterByType(selectedType),
                    )
                }
            }
            is TypeFilterChanged -> {
                updateViewState {
                    copy(
                        selectedType = viewEvent.type,
                        results = allResults.filterByMarket(selectedMarket).filterByType(viewEvent.type),
                    )
                }
            }
            is FavouriteToggled -> toggleFavourite(viewEvent.ticker)
            is SecurityClicked -> viewModelScope.emitSideEffect(
                SearchSideEffect.Navigation.NavigateToSecurity(viewEvent.ticker)
            )
            BackClicked -> viewModelScope.emitSideEffect(SearchSideEffect.Navigation.NavigateBack)
        }
    }

    private fun observeSearch() {
        viewModelScope.launch {
            queryFlow.debounce(250L).collectLatest { query ->
                if (query.isBlank()) {
                    allResults = emptyList()
                    updateViewState { copy(results = emptyList(), isLoading = false, error = null) }
                    return@collectLatest
                }
                updateViewState { copy(isLoading = true, error = null) }
                val market = viewState.value.selectedMarket
                searchSecurities(query, market.region)
                    .onSuccess { results ->
                        allResults = results
                        val type = viewState.value.selectedType
                        updateViewState { copy(results = results.filterByMarket(market).filterByType(type), isLoading = false) }
                    }
                    .onFailure { e ->
                        allResults = emptyList()
                        updateViewState { copy(results = emptyList(), isLoading = false, error = e.message) }
                    }
            }
        }
    }

    private fun List<StockQuote>.filterByMarket(market: ExchangeMarket): List<StockQuote> =
        if (market == ExchangeMarket.ALL) this else filter { market.matches(it.exchange) }

    private fun List<StockQuote>.filterByType(type: SecurityType?): List<StockQuote> =
        if (type == null) this else filter { it.type == type }

    private fun observeWatchlist() {
        viewModelScope.launch {
            getWatchlist().collect { entries ->
                val tickers = entries.map { it.tickerId }.toSet()
                updateViewState { copy(watchlistedTickers = tickers) }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            var previousConnected = true
            connectivityManager.observeConnectivity().collect { isConnected ->
                if (!previousConnected && isConnected) {
                    val current = queryFlow.value
                    queryFlow.value = ""
                    queryFlow.value = current
                }
                previousConnected = isConnected
            }
        }
    }

    private fun toggleFavourite(ticker: String) {
        viewModelScope.launch {
            if (ticker in viewState.value.watchlistedTickers) {
                removeFromWatchlist(ticker)
            } else {
                addToWatchlist(ticker)
            }
        }
    }
}
