package com.akole.dividox.feature.dividends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.feature.dividends.DividendsContract.DividendsSideEffect
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewEvent
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewState
import com.akole.dividox.integration.dividend.domain.model.DividendActivitySummary
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import com.akole.dividox.integration.dividend.domain.model.MonthBar
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendActivitySummaryUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendProjectionBarsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedPaymentHistoryUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedUpcomingPaymentsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.ObservePortfolioChangesUseCase
import com.akole.dividox.integration.dividend.domain.usecase.SyncDividendHistoryFromHoldingsUseCase
import com.akole.dividox.common.ui.resources.di.todayIn
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.until

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
) : ViewModel(),
    MVI<DividendsViewState, DividendsViewEvent, DividendsSideEffect> by mvi(DividendsViewState()) {

    private var dataJob: Job? = null

    /** Full unfiltered bar list — ViewModel filters by [DividendsViewState.selectedRange]. */
    private var allBars: List<MonthBar> = emptyList()

    init {
        observePortfolioAndSync()
        observeData()
        observeConnectivity()
    }

    /**
     * Observes portfolio ticker changes. Each time the set of tickers changes (new holding added
     * or removed), a full dividend sync is triggered so the screen reflects up-to-date data.
     */
    private fun observePortfolioAndSync() {
        viewModelScope.launch {
            observePortfolioChanges().collect { holdings ->
                syncDividendHistory(holdings).onFailure { e ->
                    updateViewState { copy(error = "Sync failed: ${e.message}") }
                }
            }
        }
    }

    override fun onViewEvent(viewEvent: DividendsViewEvent) {        when (viewEvent) {
            DividendsViewEvent.Refresh -> observeData()
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
                allBars = bars.convertBarAmounts(targetCurrency)
                val today = todayIn(TimeZone.UTC)
                val endOfYear = LocalDate(today.year, Month.DECEMBER, 31)
                val convertedHistory = history.convertPaymentAmounts(targetCurrency)
                // DB-confirmed upcoming (from Yahoo Finance future events, if any)
                val dbConfirmed = upcoming
                    .filter { it.payment.paymentDate in today..endOfYear }
                    .convertPaymentAmounts(targetCurrency)
                val dbConfirmedTickers = dbConfirmed.map { it.payment.tickerId }.toSet()
                // Projected upcoming derived from dividend history intervals
                val projected = projectUpcomingPayments(convertedHistory, today, endOfYear)
                val allUpcoming = (dbConfirmed + projected.filter { it.payment.tickerId !in dbConfirmedTickers })
                    .sortedBy { it.payment.paymentDate }
                val historyByMonth = convertedHistory.groupByMonth()
                val mostRecentMonth = historyByMonth.keys.maxOrNull()
                val expandedMonths = if (mostRecentMonth != null) setOf(mostRecentMonth) else emptySet()
                val convertedSummary = summary.convertSummaryAmounts(targetCurrency)
                    ?.let { s -> s.copy(nextPayout = allUpcoming.firstOrNull() ?: s.nextPayout) }
                viewState.value.copy(
                    isLoading = false,
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

    private suspend fun List<MonthBar>.convertBarAmounts(to: Currency): List<MonthBar> {
        if (to == Currency.USD) return this
        val rate = currencyConverter.getRate(Currency.USD, to).getOrNull() ?: return this
        return map { bar -> bar.copy(amount = bar.amount * rate) }
    }

    private suspend fun DividendActivitySummary?.convertSummaryAmounts(to: Currency): DividendActivitySummary? {
        if (this == null || to == Currency.USD) return this
        val rate = currencyConverter.getRate(Currency.USD, to).getOrNull() ?: return this
        return copy(
            lifetime = lifetime * rate,
            ytd = ytd * rate,
        )
    }

    private suspend fun List<EnrichedPayment>.convertPaymentAmounts(to: Currency): List<EnrichedPayment> {
        if (to == Currency.USD) return this
        val sourceCurrencies = mapNotNull { enriched ->
            Currency.entries.firstOrNull { it.code == enriched.payment.currency }
        }.toSet()
        val rateMap: Map<Currency, Double> = sourceCurrencies
            .filter { it != to }
            .associateWith { from ->
                currencyConverter.getRate(from, to).getOrNull() ?: 1.0
            }
        return map { enriched ->
            val from = Currency.entries.firstOrNull { it.code == enriched.payment.currency }
                ?: Currency.USD
            val rate = rateMap[from] ?: return@map enriched
            enriched.copy(payment = enriched.payment.copy(amount = enriched.payment.amount * rate))
        }
    }

    private fun projectUpcomingPayments(
        history: List<EnrichedPayment>,
        today: LocalDate,
        endOfYear: LocalDate,
    ): List<EnrichedPayment> {
        val projected = mutableListOf<EnrichedPayment>()
        history.groupBy { it.payment.tickerId }.forEach { (_, payments) ->
            val sorted = payments.sortedBy { it.payment.paymentDate }
            if (sorted.size < 2) return@forEach
            val last = sorted.last()
            val secondLast = sorted[sorted.size - 2]
            val intervalDays = secondLast.payment.paymentDate.until(last.payment.paymentDate, DateTimeUnit.DAY)
            if (intervalDays <= 0) return@forEach
            // Advance first projected date from last payment until it reaches today or later
            var nextDate = last.payment.paymentDate.plus(intervalDays, DateTimeUnit.DAY)
            while (nextDate < today) nextDate = nextDate.plus(intervalDays, DateTimeUnit.DAY)
            // Emit projected payments until end of year (max 3 per ticker)
            var count = 0
            while (nextDate <= endOfYear && count < 3) {
                projected += EnrichedPayment(
                    payment = last.payment.copy(
                        id = DividendPaymentId("${last.payment.tickerId}-proj-$nextDate"),
                        paymentDate = nextDate,
                    ),
                    companyInfo = last.companyInfo,
                )
                nextDate = nextDate.plus(intervalDays, DateTimeUnit.DAY)
                count++
            }
        }
        return projected
    }
}
