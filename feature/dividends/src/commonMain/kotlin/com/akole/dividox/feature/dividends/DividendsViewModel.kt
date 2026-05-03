package com.akole.dividox.feature.dividends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.feature.dividends.DividendsContract.DividendsSideEffect
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewEvent
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewState
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import com.akole.dividox.integration.dividend.domain.model.MonthBar
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendActivitySummaryUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendProjectionBarsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedPaymentHistoryUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedUpcomingPaymentsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.SyncDividendHistoryFromHoldingsUseCase
import com.akole.dividox.common.ui.resources.di.todayIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

class DividendsViewModel(
    private val getDividendActivitySummary: GetDividendActivitySummaryUseCase,
    private val getDividendProjectionBars: GetDividendProjectionBarsUseCase,
    private val getEnrichedUpcomingPayments: GetEnrichedUpcomingPaymentsUseCase,
    private val getEnrichedPaymentHistory: GetEnrichedPaymentHistoryUseCase,
    private val syncDividendHistory: SyncDividendHistoryFromHoldingsUseCase,
    private val connectivityManager: NetworkConnectivityManager,
) : ViewModel(),
    MVI<DividendsViewState, DividendsViewEvent, DividendsSideEffect> by mvi(DividendsViewState()) {

    private var dataJob: Job? = null

    /** Full unfiltered bar list — ViewModel filters by [DividendsViewState.selectedRange]. */
    private var allBars: List<MonthBar> = emptyList()

    init {
        viewModelScope.launch {
            syncDividendHistory().onFailure { e ->
                updateViewState { copy(error = "Sync failed: ${e.message}") }
            }
        }
        observeData()
        observeConnectivity()
    }

    override fun onViewEvent(viewEvent: DividendsViewEvent) {
        when (viewEvent) {
            DividendsViewEvent.Refresh -> observeData()
            is DividendsViewEvent.RangeSelected -> applyRange(viewEvent.range)
            is DividendsViewEvent.MonthToggled -> toggleMonth(viewEvent.yearMonth)
            is DividendsViewEvent.PaymentClicked -> navigateToSecurity(viewEvent.ticker)
            is DividendsViewEvent.HistoryEntryClicked -> navigateToSecurity(viewEvent.ticker)
        }
    }

    private fun observeData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            combine(
                getDividendActivitySummary(),
                getDividendProjectionBars(),
                getEnrichedUpcomingPayments(),
                getEnrichedPaymentHistory(),
            ) { summary, bars, upcoming, history ->
                allBars = bars
                val historyByMonth = history.groupByMonth()
                val mostRecentMonth = historyByMonth.keys.maxOrNull()
                val expandedMonths = if (mostRecentMonth != null) setOf(mostRecentMonth) else emptySet()
                viewState.value.copy(
                    isLoading = false,
                    summary = summary,
                    projectionBars = bars.filterByRange(viewState.value.selectedRange),
                    upcomingPayments = upcoming,
                    historyByMonth = historyByMonth,
                    expandedMonths = expandedMonths,
                    error = null,
                    isOffline = false,
                )
            }.catch { e ->
                updateViewState { copy(isLoading = false, error = e.message ?: "Unknown error") }
            }.collect { newState ->
                updateViewState { newState }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            var previousConnected = true
            connectivityManager.observeConnectivity().collect { isConnected ->
                updateViewState { copy(isOffline = !isConnected) }
                if (!previousConnected && isConnected) {
                    observeData()
                }
                previousConnected = isConnected
            }
        }
    }

    private fun applyRange(range: DividendHistoryRange) {
        updateViewState {
            copy(
                selectedRange = range,
                projectionBars = allBars.filterByRange(range),
            )
        }
    }

    private fun toggleMonth(yearMonth: LocalDate) {
        updateViewState {
            val updated = if (yearMonth in expandedMonths) {
                expandedMonths - yearMonth
            } else {
                expandedMonths + yearMonth
            }
            copy(expandedMonths = updated)
        }
    }

    private fun navigateToSecurity(ticker: String) {
        viewModelScope.emitSideEffect(
            DividendsSideEffect.Navigation.NavigateToSecurity(ticker),
        )
    }

    private fun List<EnrichedPayment>.groupByMonth(): Map<LocalDate, List<EnrichedPayment>> =
        groupBy { LocalDate(it.payment.paymentDate.year, it.payment.paymentDate.month, 1) }
            .entries
            .sortedByDescending { it.key }
            .associate { it.key to it.value }

    private fun List<MonthBar>.filterByRange(range: DividendHistoryRange): List<MonthBar> {
        val today = todayIn(TimeZone.currentSystemDefault())
        val cutoff: LocalDate = when (range) {
            DividendHistoryRange.YTD -> LocalDate(today.year, Month.JANUARY, 1)
            DividendHistoryRange.ONE_YEAR -> today.minus(12, DateTimeUnit.MONTH)
            DividendHistoryRange.TWO_YEARS -> today.minus(24, DateTimeUnit.MONTH)
            DividendHistoryRange.FIVE_YEARS -> today.minus(60, DateTimeUnit.MONTH)
            DividendHistoryRange.MAX -> LocalDate(1970, Month.JANUARY, 1)
        }
        val filtered = filter { it.yearMonth >= cutoff }
        return when (range) {
            DividendHistoryRange.FIVE_YEARS, DividendHistoryRange.MAX -> filtered.aggregateByYear()
            else -> filtered
        }
    }

    private fun List<MonthBar>.aggregateByYear(): List<MonthBar> =
        groupBy { it.yearMonth.year }
            .entries
            .sortedBy { it.key }
            .map { (year, bars) ->
                MonthBar(
                    yearMonth = LocalDate(year, Month.JANUARY, 1),
                    amount = bars.sumOf { it.amount },
                    isProjected = bars.any { it.isProjected },
                )
            }
}
