package com.akole.dividox.feature.portfolio

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState
import com.akole.dividox.common.ui.resources.Currency
import com.akole.dividox.integration.security.domain.model.SecurityHolding

interface PortfolioContract {

    data class PortfolioViewState(
        val isLoading: Boolean = true,
        val holdings: List<SecurityHolding> = emptyList(),
        val searchQuery: String = "",
        val sortOrder: SortOrder = SortOrder(),
        val currency: Currency = Currency.EUR,
        val error: String? = null,
    ) : ViewState

    sealed interface PortfolioViewEvent : ViewEvent {
        data class SearchQueryChanged(val query: String) : PortfolioViewEvent
        data class SortOrderChanged(val order: SortOrder) : PortfolioViewEvent
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
    YIELD,
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
