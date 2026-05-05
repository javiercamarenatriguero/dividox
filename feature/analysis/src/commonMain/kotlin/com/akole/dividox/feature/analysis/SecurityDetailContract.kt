package com.akole.dividox.feature.analysis

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

interface SecurityDetailContract {

    data class SecurityDetailViewState(
        val ticker: String = "",
        val isLoading: Boolean = true,
        val isRefreshing: Boolean = false,
        val lastUpdated: Instant? = null,
        val currency: Currency = Currency.USD,
        val quote: StockQuote? = null,
        val companyInfo: CompanyInfo? = null,
        val dividendInfo: DividendInfo? = null,
        val isFavorite: Boolean = false,
        val selectedChartPeriod: ChartPeriod = ChartPeriod.ONE_YEAR,
        val priceHistory: List<PricePoint> = emptyList(),
        val renderedChartPeriod: ChartPeriod = ChartPeriod.ONE_YEAR,
        val isDividendChartPercentage: Boolean = false,
        val dividendGrowthData: List<DividendGrowthBar> = emptyList(),
        val isInPortfolio: Boolean = false,
        val holdingId: HoldingId? = null,
        val error: String? = null,
    ) : ViewState

    data class DividendGrowthBar(
        val year: Int,
        val absoluteValue: Double,
        val percentageOfPrice: Double,
    )

    sealed interface SecurityDetailViewEvent : ViewEvent {
        data object OnLoad : SecurityDetailViewEvent
        data object OnRefresh : SecurityDetailViewEvent
        data object OnBackClicked : SecurityDetailViewEvent
        data object OnFavoriteToggled : SecurityDetailViewEvent
        data class ChartPeriodSelected(val period: ChartPeriod) : SecurityDetailViewEvent
        data object ToggleDividendChartMode : SecurityDetailViewEvent
        data object OnAddSecurityClicked : SecurityDetailViewEvent
        data object OnEditHoldingClicked : SecurityDetailViewEvent
    }

    sealed interface SecurityDetailSideEffect : SideEffect {
        sealed interface Navigation : SecurityDetailSideEffect {
            data object NavigateBack : Navigation
            data class NavigateToAddSecurity(val ticker: String) : Navigation
            data class NavigateToEditHolding(val holdingId: HoldingId) : Navigation
        }

        sealed interface Message : SecurityDetailSideEffect {
            data class ShowError(val message: String) : Message
            data class ShowSuccess(val message: String) : Message
        }
    }
}
