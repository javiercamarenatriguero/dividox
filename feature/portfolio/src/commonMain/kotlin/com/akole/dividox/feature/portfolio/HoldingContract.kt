package com.akole.dividox.feature.portfolio

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.ui.resources.components.ExchangeMarket
import com.akole.dividox.component.market.domain.model.SecurityType
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.model.Holding

object HoldingContract {

    enum class Mode {
        ADD, EDIT
    }

    data class HoldingViewState(
        val mode: Mode = Mode.ADD,
        val holdingId: HoldingId? = null,
        val originalHolding: Holding? = null,
        val searchQuery: String = "",
        val searchResults: List<StockQuote> = emptyList(),
        val selectedSecurity: StockQuote? = null,
        val shares: String = "",
        val pricePerShare: String = "",
        val currency: Currency = Currency.USD,
        val purchaseDateMillis: Long = kotlin.time.Clock.System.now().toEpochMilliseconds(),
        val estimatedTotal: Double = 0.0,
        val selectedMarket: ExchangeMarket = ExchangeMarket.ALL,
        val selectedType: SecurityType? = null,
        val isSearching: Boolean = false,
        val isSaving: Boolean = false,
        val error: String? = null,
        val showDeleteConfirmation: Boolean = false,
        val operationCompleted: Boolean = false,
        val operationIsDelete: Boolean = false,
    )

    sealed interface HoldingViewEvent {
        // Initialization (EDIT mode only)
        data class LoadHolding(val holdingId: HoldingId) : HoldingViewEvent

        // Search & selection
        data class SearchQueryChanged(val query: String) : HoldingViewEvent
        data class MarketFilterChanged(val market: ExchangeMarket) : HoldingViewEvent
        data class TypeFilterChanged(val type: SecurityType?) : HoldingViewEvent
        data class SecuritySelected(val security: StockQuote) : HoldingViewEvent

        // Form inputs
        data class SharesChanged(val shares: String) : HoldingViewEvent
        data class PricePerShareChanged(val price: String) : HoldingViewEvent
        data class CurrencyChanged(val currency: Currency) : HoldingViewEvent
        data class PurchaseDateChanged(val dateMillis: Long) : HoldingViewEvent

        // Actions
        data object ConfirmClicked : HoldingViewEvent // "Add Position" or "Update Position"
        data object DeleteClicked : HoldingViewEvent // EDIT mode only
        data object ConfirmDeleteClicked : HoldingViewEvent
        data object CancelDeleteClicked : HoldingViewEvent
        data object DismissClicked : HoldingViewEvent
    }

    sealed interface HoldingSideEffect {
        data object PositionSaved : HoldingSideEffect // ADD or UPDATE
        data object PositionDeleted : HoldingSideEffect
        data class ShowError(val message: String) : HoldingSideEffect
        data object HapticFeedback : HoldingSideEffect
    }
}
