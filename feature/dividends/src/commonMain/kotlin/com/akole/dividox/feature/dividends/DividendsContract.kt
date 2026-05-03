package com.akole.dividox.feature.dividends

import com.akole.dividox.common.mvi.SideEffect
import com.akole.dividox.common.mvi.ViewEvent
import com.akole.dividox.common.mvi.ViewState
import com.akole.dividox.integration.dividend.domain.model.DividendActivitySummary
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import com.akole.dividox.integration.dividend.domain.model.MonthBar
import kotlinx.datetime.LocalDate

interface DividendsContract {

    data class DividendsViewState(
        val isLoading: Boolean = true,
        val summary: DividendActivitySummary? = null,
        val projectionBars: List<MonthBar> = emptyList(),
        val upcomingPayments: List<EnrichedPayment> = emptyList(),
        val historyByMonth: Map<LocalDate, List<EnrichedPayment>> = emptyMap(),
        val expandedMonths: Set<LocalDate> = emptySet(),
        val error: String? = null,
        val isOffline: Boolean = false,
    ) : ViewState

    sealed interface DividendsViewEvent : ViewEvent {
        data object Refresh : DividendsViewEvent
        data class MonthToggled(val yearMonth: LocalDate) : DividendsViewEvent
        data class PaymentClicked(val ticker: String) : DividendsViewEvent
        data class HistoryEntryClicked(val ticker: String) : DividendsViewEvent
    }

    sealed interface DividendsSideEffect : SideEffect {
        sealed interface Navigation : DividendsSideEffect {
            data class NavigateToSecurity(val ticker: String) : Navigation
        }
    }
}
