package com.akole.dividox.feature.dividends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.feature.dividends.DividendsContract.DividendsSideEffect
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewEvent
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewState
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendActivitySummaryUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendProjectionBarsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedPaymentHistoryUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedUpcomingPaymentsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class DividendsViewModel(
    private val getDividendActivitySummary: GetDividendActivitySummaryUseCase,
    private val getDividendProjectionBars: GetDividendProjectionBarsUseCase,
    private val getEnrichedUpcomingPayments: GetEnrichedUpcomingPaymentsUseCase,
    private val getEnrichedPaymentHistory: GetEnrichedPaymentHistoryUseCase,
    private val connectivityManager: NetworkConnectivityManager,
) : ViewModel(),
    MVI<DividendsViewState, DividendsViewEvent, DividendsSideEffect> by mvi(DividendsViewState()) {

    private var dataJob: Job? = null

    init {
        observeData()
        observeConnectivity()
    }

    override fun onViewEvent(viewEvent: DividendsViewEvent) {
        when (viewEvent) {
            DividendsViewEvent.Refresh -> observeData()
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
                val historyByMonth = history.groupByMonth()
                val mostRecentMonth = historyByMonth.keys.maxOrNull()
                val expandedMonths = if (mostRecentMonth != null) setOf(mostRecentMonth) else emptySet()
                viewState.value.copy(
                    isLoading = false,
                    summary = summary,
                    projectionBars = bars,
                    upcomingPayments = upcoming,
                    historyByMonth = historyByMonth,
                    expandedMonths = expandedMonths,
                    error = null,
                    isOffline = false,
                )
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
            .toSortedMap(compareByDescending { it })
}
