package com.akole.dividox.feature.portfolio

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.integration.security.domain.model.PortfolioValuePoint
import com.akole.dividox.integration.security.domain.model.SecurityHolding

interface PortfolioContract {

    data class PortfolioViewState(
        val isLoading: Boolean = true,
        val holdings: List<SecurityHolding> = emptyList(),
        val searchQuery: String = "",
        val sortOrder: SortOrder = SortOrder(),
        val currency: Currency = Currency.EUR,
        val error: String? = null,
        val convertedPrices: Map<String, Double> = emptyMap(),
        val evolutionPeriod: ChartPeriod = DEFAULT_EVOLUTION_PERIOD,
        val evolution: List<PortfolioValuePoint> = emptyList(),
        val isEvolutionLoading: Boolean = true,
    ) : ViewState {
        companion object {
            val DEFAULT_EVOLUTION_PERIOD = ChartPeriod.ONE_YEAR
            val EVOLUTION_PERIODS = listOf(ChartPeriod.ONE_MONTH, ChartPeriod.ONE_YEAR, ChartPeriod.FIVE_YEARS)
        }
    }

    sealed interface PortfolioViewEvent : ViewEvent {
        data class SearchQueryChanged(val query: String) : PortfolioViewEvent
        data class SortOrderChanged(val order: SortOrder) : PortfolioViewEvent
        data class EvolutionPeriodSelected(val period: ChartPeriod) : PortfolioViewEvent
        data object AddHoldingClicked : PortfolioViewEvent
        data class EditHoldingClicked(val holdingId: String) : PortfolioViewEvent
        data class SecurityClicked(val ticker: String) : PortfolioViewEvent
    }

    sealed interface PortfolioSideEffect : SideEffect {
        sealed interface Navigation : PortfolioSideEffect {
            data object NavigateToAddHolding : Navigation
            data class NavigateToEditHolding(val holdingId: String) : Navigation
            data class NavigateToSecurity(val ticker: String) : Navigation
        }
    }
}

enum class SortField {
    GAIN,
    VALUE,
    DIVIDEND,
    DATE,
}

data class SortOrder(
    val field: SortField = SortField.GAIN,
    val ascending: Boolean = false,
) {
    fun toggle(clickedField: SortField): SortOrder =
        if (field == clickedField) copy(ascending = !ascending)
        else SortOrder(field = clickedField, ascending = false)
}
