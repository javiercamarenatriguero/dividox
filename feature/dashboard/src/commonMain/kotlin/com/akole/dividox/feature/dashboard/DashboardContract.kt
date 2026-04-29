package com.akole.dividox.feature.dashboard

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry
import com.akole.dividox.integration.security.domain.model.PortfolioSummary

interface DashboardContract {

    data class DashboardViewState(
        val isLoading: Boolean = true,
        val summary: PortfolioSummary? = null,
        val watchlist: List<EnrichedWatchlistEntry> = emptyList(),
        val selectedPeriod: ChartPeriod = ChartPeriod.ONE_MONTH,
        val showInEur: Boolean = false,
        val error: String? = null,
    ) : ViewState

    sealed interface DashboardViewEvent : ViewEvent {
        data class PeriodSelected(val period: ChartPeriod) : DashboardViewEvent
        data object CurrencyToggled : DashboardViewEvent
        data class FavouriteToggled(val ticker: String) : DashboardViewEvent
        data class SecurityClicked(val ticker: String) : DashboardViewEvent
        data object ViewAllFavouritesClicked : DashboardViewEvent
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
    ALL("ALL"),
}
