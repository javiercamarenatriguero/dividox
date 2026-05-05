package com.akole.dividox.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesSideEffect
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent.BackClicked
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent.FavoriteToggled
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent.SearchQueryChanged
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent.SecurityClicked
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewState
import com.akole.dividox.integration.security.domain.usecase.GetEnrichedWatchlistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val getEnrichedWatchlist: GetEnrichedWatchlistUseCase,
    private val removeFromWatchlist: RemoveFromWatchlistUseCase,
    private val observeAppSettings: ObserveAppSettingsUseCase,
    private val currencyConverter: CurrencyConverter,
    private val connectivityManager: NetworkConnectivityManager,
) : ViewModel(),
    MVI<FavoritesViewState, FavoritesViewEvent, FavoritesSideEffect> by mvi(FavoritesViewState()) {

    private val searchQuery = MutableStateFlow("")

    init {
        observeData()
        observeConnectivity()
    }

    override fun onViewEvent(viewEvent: FavoritesViewEvent) {
        when (viewEvent) {
            is SearchQueryChanged -> searchQuery.value = viewEvent.query
            is FavoriteToggled -> removeFavorite(viewEvent.ticker)
            is SecurityClicked -> viewModelScope.emitSideEffect(
                FavoritesSideEffect.Navigation.NavigateToSecurity(viewEvent.ticker)
            )
            BackClicked -> viewModelScope.emitSideEffect(FavoritesSideEffect.Navigation.NavigateBack)
        }
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                getEnrichedWatchlist(),
                searchQuery,
                observeAppSettings(),
            ) { entries, query, settings ->
                val targetCurrency = settings.currency
                val filtered = if (query.isBlank()) entries else {
                    val lower = query.lowercase()
                    entries.filter { e ->
                        e.entry.tickerId.lowercase().contains(lower) ||
                            e.companyInfo?.name?.lowercase()?.contains(lower) == true
                    }
                }
                val convertedPrices = currencyConverter.convertWatchlistPrices(filtered, targetCurrency)
                viewState.value.copy(
                    isLoading = false,
                    favorites = filtered,
                    searchQuery = query,
                    currency = targetCurrency,
                    convertedPrices = convertedPrices,
                    error = null,
                )
            }.catch { e ->
                updateViewState { copy(isLoading = false, error = e.message ?: "Unknown error") }
            }.collect { newState ->
                updateViewState { newState }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            var previousConnected = true
            connectivityManager.observeConnectivity().collect { isConnected ->
                if (!previousConnected && isConnected) observeData()
                previousConnected = isConnected
            }
        }
    }

    private fun removeFavorite(ticker: String) {
        viewModelScope.launch {
            removeFromWatchlist(ticker)
        }
    }
}
