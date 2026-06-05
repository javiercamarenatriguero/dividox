package com.akole.dividox.feature.dashboard

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.components.NewsItemUi
import com.akole.dividox.component.market.domain.model.MarketIndexQuote
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry
import com.akole.dividox.integration.security.domain.model.PortfolioSummary
import kotlin.time.Instant

interface DashboardContract {

    data class DashboardViewState(
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val lastUpdated: Instant? = null,
        val summary: PortfolioSummary? = null,
        val watchlist: List<EnrichedWatchlistEntry> = emptyList(),
        val selectedPeriod: ChartPeriod = ChartPeriod.ONE_DAY,
        val currency: Currency = Currency.EUR,
        val error: String? = null,
        val convertedSummary: PortfolioSummary? = null,
        val convertedWatchlistPrices: Map<String, Double> = emptyMap(),
        val periodGainPercent: Double = 0.0,
        val periodGainAbsolute: Double = 0.0,
        val periodDividends: Double = 0.0,
        val lifetimeDividends: Double = 0.0,
        val totalGainPercent: Double = 0.0,
        val totalGainAbsolute: Double = 0.0,
        val topGainers: List<PortfolioTodayItem> = emptyList(),
        val topLosers: List<PortfolioTodayItem> = emptyList(),
        val marketIndices: List<MarketIndexQuote> = emptyList(),
        val marketIndicesLoading: Boolean = true,
        val marketIndicesError: Boolean = false,
        val marketNews: List<NewsItemUi> = emptyList(),
        val marketNewsLoading: Boolean = false,
    ) : ViewState

    sealed interface DashboardViewEvent : ViewEvent {
        data class PeriodSelected(val period: ChartPeriod) : DashboardViewEvent
        data class CurrencySelected(val currency: Currency) : DashboardViewEvent
        data class FavouriteToggled(val ticker: String) : DashboardViewEvent
        data class SecurityClicked(val ticker: String) : DashboardViewEvent
        data object ViewAllFavouritesClicked : DashboardViewEvent
        data object ViewAllPortfolioClicked : DashboardViewEvent
        data object Refresh : DashboardViewEvent
    }

    sealed interface DashboardSideEffect : SideEffect {
        sealed interface Navigation : DashboardSideEffect {
            data class NavigateToSecurity(val ticker: String) : Navigation
            data object NavigateToFavorites : Navigation
            data object NavigateToPortfolio : Navigation
        }
    }
}

data class PortfolioTodayItem(
    val ticker: String,
    val name: String?,
    val changePercent: Double,
    val price: Double,
    val currency: Currency,
)

enum class ChartPeriod {
    ONE_DAY,
    ONE_WEEK,
    ONE_MONTH,
    ONE_YEAR,
    YEAR_TO_DATE,
}
