package com.akole.dividox.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.AppRefreshTracker
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.settings.domain.usecase.SetCurrencyUseCase
import com.akole.dividox.component.market.domain.model.ChartPeriod as MarketChartPeriod
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardSideEffect
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.CurrencySelected
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.FavouriteToggled
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.PeriodSelected
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.Refresh
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.SecurityClicked
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.ViewAllFavouritesClicked
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewState
import com.akole.dividox.integration.dividend.domain.usecase.GetPeriodDividendsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.ObservePortfolioChangesUseCase
import com.akole.dividox.integration.dividend.domain.usecase.SyncDividendHistoryFromHoldingsUseCase
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry
import com.akole.dividox.integration.security.domain.model.PortfolioSummary
import com.akole.dividox.integration.security.domain.usecase.GetEnrichedWatchlistUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioPeriodGainUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioSummaryUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.Clock
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

@Suppress("LongParameterList", "TooManyFunctions")
class DashboardViewModel(
    private val getPortfolioWithQuotes: GetPortfolioWithQuotesUseCase,
    private val getPortfolioSummary: GetPortfolioSummaryUseCase,
    private val getPortfolioPeriodGain: GetPortfolioPeriodGainUseCase,
    private val getPeriodDividends: GetPeriodDividendsUseCase,
    private val getEnrichedWatchlist: GetEnrichedWatchlistUseCase,
    private val removeFromWatchlist: RemoveFromWatchlistUseCase,
    private val observeAppSettings: ObserveAppSettingsUseCase,
    private val setCurrency: SetCurrencyUseCase,
    private val currencyConverter: CurrencyConverter,
    private val connectivityManager: NetworkConnectivityManager,
    private val refreshTracker: AppRefreshTracker,
    private val observePortfolioChanges: ObservePortfolioChangesUseCase,
    private val syncDividendHistory: SyncDividendHistoryFromHoldingsUseCase,
) : ViewModel(),
    MVI<DashboardViewState, DashboardViewEvent, DashboardSideEffect> by mvi(DashboardViewState()) {

    private var dataJob: Job? = null
    private val periodFlow = MutableStateFlow(ChartPeriod.ALL)
    // Internal flows updated by separate coroutines so slow API calls don't block main UI data
    private val periodGainFlow = MutableStateFlow(0.0 to 0.0) // (absoluteUsd, percent)
    private val periodDividendsFlow = MutableStateFlow(0.0)   // USD, unconverted
    private val lifetimeDividendsFlow = MutableStateFlow(0.0) // USD, unconverted

    init {
        observePortfolioAndSync()
        observeData()
        observeConnectivity()
        observeRefreshTracker()
    }

    private fun observePortfolioAndSync() {
        viewModelScope.launch {
            observePortfolioChanges().collect { holdings ->
                syncDividendHistory(holdings)
            }
        }
    }

    override fun onViewEvent(viewEvent: DashboardViewEvent) {
        when (viewEvent) {
            is PeriodSelected -> {
                updateViewState { copy(selectedPeriod = viewEvent.period) }
                periodFlow.value = viewEvent.period
            }
            is CurrencySelected -> selectCurrency(viewEvent.currency)
            is FavouriteToggled -> removeFavourite(viewEvent.ticker)
            is SecurityClicked -> viewModelScope.emitSideEffect(
                DashboardSideEffect.Navigation.NavigateToSecurity(viewEvent.ticker),
            )
            ViewAllFavouritesClicked -> viewModelScope.emitSideEffect(
                DashboardSideEffect.Navigation.NavigateToFavorites,
            )
            Refresh -> {
                updateViewState { copy(isRefreshing = true) }
                observeData()
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            // 1. Period dividends — Room query, fast, restarts on period change
            launch {
                periodFlow.flatMapLatest { period ->
                    getPeriodDividends(period.toStartDate())
                }.collect { dividends ->
                    periodDividendsFlow.value = dividends
                }
            }

            // 1b. Lifetime dividends — collected once, updates reactively
            launch {
                getPeriodDividends(null).collect { lifetime ->
                    lifetimeDividendsFlow.value = lifetime
                }
            }

            // 2. Period gain — potentially slow API calls, cancelled on new emission (collectLatest)
            launch {
                combine(getPortfolioWithQuotes(), periodFlow) { holdings, period ->
                    holdings to period
                }.collectLatest { (holdings, period) ->
                    val (abs, pct) = getPortfolioPeriodGain(holdings, period.toMarketPeriod())
                    periodGainFlow.value = abs to pct
                }
            }

            // 3. Main combine — period-independent data + derived period StateFlows, always fast
            val dividendPairFlow =
                combine(periodDividendsFlow, lifetimeDividendsFlow) { period, lifetime -> period to lifetime }
            combine(
                getPortfolioSummary(),
                getEnrichedWatchlist(),
                observeAppSettings(),
                dividendPairFlow,
                periodGainFlow,
            ) { summary, watchlist, settings, (dividends, lifetime), (gainAbs, gainPct) ->
                val targetCurrency = settings.currency
                val convertedSummary = convertSummary(summary, targetCurrency)
                val convertedWatchlistPrices = convertWatchlistPrices(watchlist, targetCurrency)
                viewState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    summary = convertedSummary,
                    watchlist = watchlist,
                    currency = targetCurrency,
                    error = null,
                    convertedSummary = convertedSummary,
                    convertedWatchlistPrices = convertedWatchlistPrices,
                    periodGainPercent = gainPct,
                    periodGainAbsolute = convertAmount(gainAbs, targetCurrency),
                    periodDividends = convertAmount(dividends, targetCurrency),
                    lifetimeDividends = convertAmount(lifetime, targetCurrency),
                )
            }.collect { newState ->
                val now = Clock.System.now()
                refreshTracker.notifyRefreshed(now)
                updateViewState { newState.copy(lastUpdated = now) }
            }
        }
    }

    private fun ChartPeriod.toStartDate(): LocalDate? {
        val today = Clock.System.todayIn(TimeZone.UTC)
        return when (this) {
            ChartPeriod.ALL -> null
            ChartPeriod.ONE_DAY -> today.minus(DatePeriod(days = 1))
            ChartPeriod.ONE_WEEK -> today.minus(DatePeriod(days = 7))
            ChartPeriod.ONE_MONTH -> today.minus(DatePeriod(months = 1))
            ChartPeriod.ONE_YEAR -> today.minus(DatePeriod(years = 1))
            ChartPeriod.YEAR_TO_DATE -> LocalDate(today.year, 1, 1)
        }
    }

    private suspend fun convertAmount(amount: Double, to: Currency): Double {
        if (to == Currency.USD) return amount
        return currencyConverter.getRate(Currency.USD, to).getOrNull()?.let { rate ->
            amount * rate
        } ?: amount
    }

    private fun ChartPeriod.toMarketPeriod(): MarketChartPeriod = when (this) {
        ChartPeriod.ONE_DAY -> MarketChartPeriod.ONE_DAY
        ChartPeriod.ONE_WEEK -> MarketChartPeriod.ONE_WEEK
        ChartPeriod.ONE_MONTH -> MarketChartPeriod.ONE_MONTH
        ChartPeriod.ONE_YEAR -> MarketChartPeriod.ONE_YEAR
        ChartPeriod.YEAR_TO_DATE -> MarketChartPeriod.YTD
        ChartPeriod.ALL -> MarketChartPeriod.ALL
    }

    private suspend fun convertSummary(summary: PortfolioSummary, to: Currency): PortfolioSummary {
        val from = Currency.USD
        if (from == to) return summary
        val rate = currencyConverter.getRate(from, to).getOrNull() ?: return summary
        return PortfolioSummary(
            totalValue = summary.totalValue * rate,
            totalGain = summary.totalGain * rate,
            totalGainPercent = summary.totalGainPercent,
            totalYield = summary.totalYield,
            dividendsCollected = summary.dividendsCollected * rate,
        )
    }

    private suspend fun convertWatchlistPrices(
        watchlist: List<EnrichedWatchlistEntry>,
        to: Currency,
    ): Map<String, Double> = buildMap {
        watchlist.forEach { entry ->
            val quote = entry.quote ?: return@forEach
            val ticker = entry.entry.tickerId
            val from = Currency.entries.firstOrNull { it.code == quote.currency } ?: Currency.USD
            val converted = currencyConverter.convert(quote.price, from, to).getOrElse { quote.price }
            put(ticker, converted)
        }
    }

    private fun selectCurrency(currency: Currency) {
        viewModelScope.launch {
            setCurrency(currency)
        }
    }

    private fun removeFavourite(ticker: String) {
        viewModelScope.launch {
            removeFromWatchlist(ticker)
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            var previousConnected = true
            connectivityManager.observeConnectivity().collect { isConnected ->
                if (!previousConnected && isConnected) {
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
}
