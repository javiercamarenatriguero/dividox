package com.akole.dividox.feature.dividends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.AppRefreshTracker
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
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
import com.akole.dividox.integration.dividend.domain.usecase.ObservePortfolioChangesUseCase
import com.akole.dividox.integration.dividend.domain.usecase.SyncDividendHistoryFromHoldingsUseCase
import com.akole.dividox.common.ui.resources.di.todayIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone

class DividendsViewModel(
    private val getDividendActivitySummary: GetDividendActivitySummaryUseCase,
    private val getDividendProjectionBars: GetDividendProjectionBarsUseCase,
    private val getEnrichedUpcomingPayments: GetEnrichedUpcomingPaymentsUseCase,
    private val getEnrichedPaymentHistory: GetEnrichedPaymentHistoryUseCase,
    private val syncDividendHistory: SyncDividendHistoryFromHoldingsUseCase,
    private val connectivityManager: NetworkConnectivityManager,
    private val observePortfolioChanges: ObservePortfolioChangesUseCase,
    private val observeAppSettings: ObserveAppSettingsUseCase,
    private val currencyConverter: CurrencyConverter,
    private val refreshTracker: AppRefreshTracker,
) : ViewModel(),
    MVI<DividendsViewState, DividendsViewEvent, DividendsSideEffect> by mvi(DividendsViewState()) {

    private var dataJob: Job? = null

    /** Full unfiltered bar list — ViewModel filters by [DividendsViewState.selectedRange]. */
    private var allBars: List<MonthBar> = emptyList()

    /** Last known holdings — used to force re-sync on connectivity recovery. */
    private var lastHoldings: List<com.akole.dividox.component.portfolio.domain.model.Holding> = emptyList()

    init {
        observePortfolioAndSync()
        observeData()
        observeConnectivity()
        observeRefreshTracker()
    }

    /**
     * Observes portfolio ticker changes. Each time the set of tickers changes (new holding added
     * or removed), a full dividend sync is triggered so the screen reflects up-to-date data.
     */
    private fun observePortfolioAndSync() {
        viewModelScope.launch {
            observePortfolioChanges().collect { holdings ->
                lastHoldings = holdings
                syncDividendHistory(holdings).onFailure { e ->
                    updateViewState { copy(error = "Sync failed: ${e.message}") }
                }
            }
        }
    }

    override fun onViewEvent(viewEvent: DividendsViewEvent) {        when (viewEvent) {
            DividendsViewEvent.Refresh -> {
                updateViewState { copy(isRefreshing = true) }
                observeData()
            }
            is DividendsViewEvent.RangeSelected -> applyRange(viewEvent.range)
            DividendsViewEvent.ToggleUpcomingExpanded -> updateViewState { copy(upcomingExpanded = !upcomingExpanded) }
            DividendsViewEvent.TogglePastActivityExpanded -> updateViewState { copy(pastActivityExpanded = !pastActivityExpanded) }
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
                observeAppSettings(),
            ) { summary, bars, upcoming, history, settings ->
                val targetCurrency = settings.currency
                allBars = currencyConverter.convertBarAmounts(bars, targetCurrency)
                val today = todayIn(TimeZone.UTC)
                val endOfYear = LocalDate(today.year, Month.DECEMBER, 31)
                val convertedHistory = currencyConverter.convertPaymentAmounts(history, targetCurrency)
                // DB-confirmed upcoming (from Yahoo Finance future events, if any)
                val dbConfirmed = currencyConverter.convertPaymentAmounts(
                    upcoming.filter { it.payment.paymentDate in today..endOfYear },
                    targetCurrency,
                )
                val dbConfirmedTickers = dbConfirmed.map { it.payment.tickerId }.toSet()
                // Projected upcoming derived from dividend history intervals
                val projected = projectUpcomingPayments(convertedHistory, today, endOfYear)
                val allUpcoming = (dbConfirmed + projected.filter { it.payment.tickerId !in dbConfirmedTickers })
                    .sortedBy { it.payment.paymentDate }
                val historyByMonth = convertedHistory.groupByMonth()
                val mostRecentMonth = historyByMonth.keys.maxOrNull()
                val expandedMonths = if (mostRecentMonth != null) setOf(mostRecentMonth) else emptySet()
                val convertedSummary = currencyConverter.convertSummaryAmounts(summary, targetCurrency)
                    ?.let { s -> s.copy(nextPayout = allUpcoming.firstOrNull() ?: s.nextPayout) }
                viewState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    currency = targetCurrency,
                    summary = convertedSummary,
                    projectionBars = allBars.filterByRange(viewState.value.selectedRange),
                    upcomingPayments = allUpcoming,
                    historyByMonth = historyByMonth,
                    expandedMonths = expandedMonths,
                    error = null,
                    isOffline = false,
                )
            }.catch { e ->
                updateViewState { copy(isLoading = false, isRefreshing = false, error = e.message ?: "Unknown error") }
            }.collect { newState ->
                val now = Clock.System.now()
                refreshTracker.notifyRefreshed(now)
                updateViewState { newState.copy(lastUpdated = now) }
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            var previousConnected = true
            connectivityManager.observeConnectivity().collect { isConnected ->
                updateViewState { copy(isOffline = !isConnected) }
                if (!previousConnected && isConnected) {
                    // Force re-sync dividends from API with current holdings
                    if (lastHoldings.isNotEmpty()) {
                        syncDividendHistory(lastHoldings)
                    }
                    observeData()
                }
                previousConnected = isConnected
            }
        }
    }

    private fun observeRefreshTracker() {
        viewModelScope.launch {
            refreshTracker.lastRefreshed.filterNotNull().collect { instant ->
                updateViewState { copy(lastUpdated = instant) }
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

}
