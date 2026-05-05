package com.akole.dividox.feature.favorites

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry

interface FavoritesContract {

    data class FavoritesViewState(
        val favorites: List<EnrichedWatchlistEntry> = emptyList(),
        val searchQuery: String = "",
        val isLoading: Boolean = true,
        val error: String? = null,
        val currency: Currency = Currency.USD,
        val convertedPrices: Map<String, Double> = emptyMap(),
    ) : ViewState

    sealed interface FavoritesViewEvent : ViewEvent {
        data class SearchQueryChanged(val query: String) : FavoritesViewEvent
        data class FavoriteToggled(val ticker: String) : FavoritesViewEvent
        data class SecurityClicked(val ticker: String) : FavoritesViewEvent
        data object BackClicked : FavoritesViewEvent
    }

    sealed interface FavoritesSideEffect : SideEffect {
        sealed interface Navigation : FavoritesSideEffect {
            data class NavigateToSecurity(val ticker: String) : Navigation
            data object NavigateBack : Navigation
        }
    }
}
