package com.akole.dividox.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.component.market.domain.usecase.SearchSecuritiesUseCase
import com.akole.dividox.component.watchlist.domain.usecase.AddToWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.GetWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.search.SearchContract.SearchSideEffect
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.BackClicked
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.FavouriteToggled
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.QueryChanged
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent.SecurityClicked
import com.akole.dividox.feature.search.SearchContract.SearchViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchSecurities: SearchSecuritiesUseCase,
    private val getWatchlist: GetWatchlistUseCase,
    private val addToWatchlist: AddToWatchlistUseCase,
    private val removeFromWatchlist: RemoveFromWatchlistUseCase,
    private val connectivityManager: NetworkConnectivityManager,
) : ViewModel(),
    MVI<SearchViewState, SearchViewEvent, SearchSideEffect> by mvi(SearchViewState()) {

    private val queryFlow = MutableStateFlow("")

    init {
        observeSearch()
        observeWatchlist()
        observeConnectivity()
    }

    override fun onViewEvent(viewEvent: SearchViewEvent) {
        when (viewEvent) {
            is QueryChanged -> {
                updateViewState { copy(query = viewEvent.query) }
                queryFlow.value = viewEvent.query
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
                    updateViewState { copy(results = emptyList(), isLoading = false, error = null) }
                    return@collectLatest
                }
                updateViewState { copy(isLoading = true, error = null) }
                searchSecurities(query)
                    .onSuccess { results ->
                        updateViewState { copy(results = results, isLoading = false) }
                    }
                    .onFailure { e ->
                        updateViewState { copy(results = emptyList(), isLoading = false, error = e.message) }
                    }
            }
        }
    }

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
