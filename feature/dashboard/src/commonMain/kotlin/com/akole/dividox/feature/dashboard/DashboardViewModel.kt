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
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardSideEffect
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.CurrencySelected
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.FavouriteToggled
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.PeriodSelected
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.Refresh
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.SecurityClicked
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.ViewAllFavouritesClicked
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent.ViewAllPortfolioClicked
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewState
import com.akole.dividox.integration.dividend.domain.usecase.GetPeriodDividendsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.ObservePortfolioChangesUseCase
import com.akole.dividox.integration.dividend.domain.usecase.SyncDividendHistoryFromHoldingsUseCase
import com.akole.dividox.integration.security.domain.usecase.GetEnrichedWatchlistUseCase
import com.akole.dividox.component.market.domain.model.ChartPeriod as MarketChartPeriod
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioPeriodGainUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioSummaryUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import com.akole.dividox.integration.security.domain.model.PortfolioSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.Clock
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

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
    private val periodFlow = MutableStateFlow(ChartPeriod.ONE_DAY)
    // Internal flows updated by separate coroutines so slow API calls don't block main UI data
    private val summaryFlow = MutableStateFlow<PortfolioSummary?>(null)         // null until portfolio API calls complete
    private val periodGainFlow = MutableStateFlow<Pair<Double, Double>?>(null)  // null until first gain result
    private val totalGainFlow = MutableStateFlow<Pair<Double, Double>?>(null)   // all-time gain, never period-affected
    private val portfolioTodayFlow = MutableStateFlow<Pair<List<SecurityHolding>, List<SecurityHolding>>>(
        emptyList<SecurityHolding>() to emptyList(),
    )
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
            ViewAllPortfolioClicked -> viewModelScope.emitSideEffect(
                DashboardSideEffect.Navigation.NavigateToPortfolio,
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
            // Single upstream shared between gain coroutine and summary — no double API calls.
            val portfolioShared = getPortfolioWithQuotes()
                .filter { it.isNotEmpty() }
                .shareIn(this, SharingStarted.WhileSubscribed(), replay = 1)

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

            // 2. Portfolio summary — slowest flow (requires all quote API calls to complete).
            // Feeds summaryFlow; isLoading stays true until first emission.
            launch {
                getPortfolioSummary(portfolioShared).collect { summary ->
                    summaryFlow.value = summary
                }
            }

            // 3. Period gain — cancelled and restarted on new portfolio or period emission.
            launch {
                combine(portfolioShared, periodFlow) { holdings, period ->
                    holdings to period
                }.collectLatest { (holdings, period) ->
                    val (abs, pct) = getPortfolioPeriodGain(holdings, period.toMarketPeriod())
                    periodGainFlow.value = abs to pct
                }
            }

            // 3b. Total (all-time) gain — fixed, independent of period selection.
            launch {
                portfolioShared.collectLatest { holdings ->
                    val (abs, pct) = getPortfolioPeriodGain(holdings, MarketChartPeriod.ALL)
                    totalGainFlow.value = abs to pct
                }
            }

            // 3c. Portfolio Today — top 3 gainers and losers by today's % change.
            launch {
                portfolioShared.collect { holdings ->
                    val gainers = holdings.filter { it.quote.changePercent > 0 }
                        .sortedByDescending { it.quote.changePercent }
                        .take(3)
                    val losers = holdings.filter { it.quote.changePercent < 0 }
                        .sortedBy { it.quote.changePercent }
                        .take(3)
                    portfolioTodayFlow.value = gainers to losers
                }
            }

            // 4. Main combine — skeleton stays until summaryFlow has its first real value.
            val dividendPairFlow =
                combine(periodDividendsFlow, lifetimeDividendsFlow) { period, lifetime -> period to lifetime }
            val allGainsAndTodayFlow = combine(
                periodGainFlow,
                totalGainFlow,
                portfolioTodayFlow,
            ) { period, total, today -> Triple(period, total, today) }
            combine(
                summaryFlow,
                getEnrichedWatchlist(),
                observeAppSettings(),
                dividendPairFlow,
                allGainsAndTodayFlow,
            ) { summary, watchlist, settings, dividendPair, gainsAndToday ->
                val (dividends, lifetime) = dividendPair
                val (periodGain, totalGain, todayPair) = gainsAndToday
                val (rawGainers, rawLosers) = todayPair
                val gainAbs = periodGain?.first ?: 0.0
                val gainPct = periodGain?.second ?: 0.0
                val totalGainAbs = totalGain?.first ?: 0.0
                val totalGainPct = totalGain?.second ?: 0.0
                val targetCurrency = settings.currency
                val convertedSummary = summary?.let { currencyConverter.convertSummary(it, targetCurrency) }
                val convertedWatchlistPrices = currencyConverter.convertWatchlistPrices(watchlist, targetCurrency)
                val topGainers = rawGainers.map { h ->
                    PortfolioTodayItem(
                        ticker = h.holding.tickerId,
                        name = h.quote.name,
                        changePercent = h.quote.changePercent,
                        price = currencyConverter.convertAmount(h.quote.price, targetCurrency),
                        currency = targetCurrency,
                    )
                }
                val topLosers = rawLosers.map { h ->
                    PortfolioTodayItem(
                        ticker = h.holding.tickerId,
                        name = h.quote.name,
                        changePercent = h.quote.changePercent,
                        price = currencyConverter.convertAmount(h.quote.price, targetCurrency),
                        currency = targetCurrency,
                    )
                }
                viewState.value.copy(
                    isLoading = summary == null || periodGain == null || totalGain == null || summary.totalValue == 0.0,
                    isRefreshing = false,
                    summary = convertedSummary,
                    watchlist = watchlist,
                    currency = targetCurrency,
                    error = null,
                    convertedSummary = convertedSummary,
                    convertedWatchlistPrices = convertedWatchlistPrices,
                    periodGainPercent = gainPct,
                    periodGainAbsolute = currencyConverter.convertAmount(gainAbs, targetCurrency),
                    periodDividends = currencyConverter.convertAmount(dividends, targetCurrency),
                    lifetimeDividends = currencyConverter.convertAmount(lifetime, targetCurrency),
                    totalGainPercent = totalGainPct,
                    totalGainAbsolute = currencyConverter.convertAmount(totalGainAbs, targetCurrency),
                    topGainers = topGainers,
                    topLosers = topLosers,
                )
            }.collect { newState ->
                val now = Clock.System.now()
                refreshTracker.notifyRefreshed(now)
                updateViewState { newState.copy(lastUpdated = now) }
            }
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
