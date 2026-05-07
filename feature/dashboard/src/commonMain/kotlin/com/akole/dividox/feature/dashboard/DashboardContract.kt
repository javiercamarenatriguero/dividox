package com.akole.dividox.feature.dashboard

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState
import com.akole.dividox.common.currency.domain.model.Currency
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
        val selectedPeriod: ChartPeriod = ChartPeriod.ONE_YEAR,
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
    ) : ViewState

    sealed interface DashboardViewEvent : ViewEvent {
        data class PeriodSelected(val period: ChartPeriod) : DashboardViewEvent
        data class CurrencySelected(val currency: Currency) : DashboardViewEvent
        data class FavouriteToggled(val ticker: String) : DashboardViewEvent
        data class SecurityClicked(val ticker: String) : DashboardViewEvent
        data object ViewAllFavouritesClicked : DashboardViewEvent
        data object Refresh : DashboardViewEvent
    }

    sealed interface DashboardSideEffect : SideEffect {
        sealed interface Navigation : DashboardSideEffect {
            data class NavigateToSecurity(val ticker: String) : Navigation
            data object NavigateToFavorites : Navigation
        }
    }
}

enum class ChartPeriod(val label: String) {
    ONE_DAY("1D"),
    ONE_WEEK("1W"),
    ONE_MONTH("1M"),
    ONE_YEAR("1Y"),
    YEAR_TO_DATE("YTD"),
}
