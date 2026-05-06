package com.akole.dividox.feature.search

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState
import com.akole.dividox.component.market.domain.model.StockQuote

interface SearchContract {

    data class SearchViewState(
        val query: String = "",
        val results: List<StockQuote> = emptyList(),
        val watchlistedTickers: Set<String> = emptySet(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedMarket: ExchangeMarket = ExchangeMarket.ALL,
    ) : ViewState

    sealed interface SearchViewEvent : ViewEvent {
        data class QueryChanged(val query: String) : SearchViewEvent
        data class FavouriteToggled(val ticker: String) : SearchViewEvent
        data class SecurityClicked(val ticker: String) : SearchViewEvent
        data class MarketFilterChanged(val market: ExchangeMarket) : SearchViewEvent
        data object BackClicked : SearchViewEvent
    }

    sealed interface SearchSideEffect : SideEffect {
        sealed interface Navigation : SearchSideEffect {
            data class NavigateToSecurity(val ticker: String) : Navigation
            data object NavigateBack : Navigation
        }
    }
}
