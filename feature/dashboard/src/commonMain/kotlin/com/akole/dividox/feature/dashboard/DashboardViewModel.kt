package com.akole.dividox.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.mvi.viewmodel.MVI
import com.akole.dividox.common.mvi.viewmodel.mvi
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.AppRefreshTracker
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.settings.domain.usecase.SetCurrencyUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.common.ui.resources.components.NewsItemUi
import com.akole.dividox.component.market.domain.model.MarketIndexQuote
import com.akole.dividox.component.market.domain.usecase.GetMajorMarketIndicesUseCase
import com.akole.dividox.component.market.domain.usecase.GetMarketNewsUseCase
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
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioPeriodGainUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioSummaryUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.time.Clock
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

@Suppress("LongParameterList", "TooManyFunctions", "LargeClass")
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
    private val getMajorMarketIndices: GetMajorMarketIndicesUseCase,
    private val getMarketNews: GetMarketNewsUseCase,
) : ViewModel(),
    MVI<DashboardViewState, DashboardViewEvent, DashboardSideEffect> by mvi(DashboardViewState()) {

    private var dataJob: Job? = null
    private var marketIndicesJob: Job? = null
    private val periodFlow = MutableStateFlow(ChartPeriod.ONE_DAY)

    // Shared settings flow — collected once and reused across every section that needs it.
    // Eagerly started so the current value is always available for combine() calls.
    private val settingsFlow: StateFlow<AppSettings?> = observeAppSettings()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        observePortfolioAndSync()
        observeData()
        observeConnectivity()
        observeRefreshTracker()
        observeMarketIndices()
        loadMarketNews()
    }

    private fun observePortfolioAndSync() {
        // Off critical path: the sync fires heavy "max range" requests per ticker that share the
        // market API semaphore, so wait until summary + yield are painted (bounded by a timeout)
        // to avoid starving the dividend-info requests the yield depends on.
        viewModelScope.launch {
            withTimeoutOrNull(SYNC_MAX_WAIT_MS) { viewState.first { !it.summaryLoading && !it.yieldLoading } }
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
                observeMarketIndices()
            }
        }
    }

    /**
     * Each section runs in its own coroutine and writes only its slice of the state.
     * Cold-start behavior:
     *   1. Cached data from Room lands almost instantly (see repository cache-first path).
     *   2. Each section flips its `*Loading` flag as its first emission arrives.
     *   3. Network refresh values overwrite silently when they arrive.
     * A single failing branch never tears down the others (supervisorScope).
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        dataJob?.cancel()
        dataJob = viewModelScope.launch {
            supervisorScope {
                val portfolioShared = getPortfolioWithQuotes()
                    .shareIn(this, SharingStarted.WhileSubscribed(), replay = 1)
                val currencyFlow = settingsFlow.filterNotNull().map { it.currency }.distinctUntilChanged()

                launch { observeWatchlist(currencyFlow) }
                launch { observePeriodDividends(currencyFlow) }
                launch { observeLifetimeDividends(currencyFlow) }
                launch { observeSummary(portfolioShared, currencyFlow) }
                launch { observePeriodGain(portfolioShared, currencyFlow) }
                launch { observePortfolioToday(portfolioShared, currencyFlow) }
            }
        }
    }

    private suspend fun observeWatchlist(currencyFlow: kotlinx.coroutines.flow.Flow<Currency>) {
        combine(getEnrichedWatchlist(), currencyFlow) { watchlist, currency ->
            watchlist to currency
        }.collect { (watchlist, currency) ->
            val convertedPrices = currencyConverter.convertWatchlistPrices(watchlist, currency)
            updateViewState {
                copy(
                    watchlist = watchlist,
                    convertedWatchlistPrices = convertedPrices,
                    watchlistLoading = false,
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observePeriodDividends(currencyFlow: kotlinx.coroutines.flow.Flow<Currency>) {
        periodFlow
            .flatMapLatest { period -> getPeriodDividends(period.toStartDate()) }
            .combine(currencyFlow) { dividends, currency -> dividends to currency }
            .collect { (dividends, currency) ->
                val converted = currencyConverter.convertAmount(dividends, currency)
                updateViewState {
                    copy(periodDividends = converted, periodDividendsLoading = false)
                }
            }
    }

    private suspend fun observeLifetimeDividends(currencyFlow: kotlinx.coroutines.flow.Flow<Currency>) {
        getPeriodDividends(null)
            .combine(currencyFlow) { lifetime, currency -> lifetime to currency }
            .collect { (lifetime, currency) ->
                val converted = currencyConverter.convertAmount(lifetime, currency)
                updateViewState {
                    copy(lifetimeDividends = converted, lifetimeDividendsLoading = false)
                }
            }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeSummary(
        portfolioShared: kotlinx.coroutines.flow.Flow<List<SecurityHolding>>,
        currencyFlow: kotlinx.coroutines.flow.Flow<Currency>,
    ) {
        // Summary is computed directly in the display currency. A null result (exchange rate
        // unavailable) is skipped so the UI keeps the last correct value instead of a mixed-currency sum.
        combine(portfolioShared, currencyFlow) { holdings, currency -> holdings to currency }
            .mapLatest { (holdings, currency) ->
                getPortfolioSummary.summarize(holdings, currency)?.let { it to currency }
            }
            .filterNotNull()
            .collect { (converted, currency) ->
                val now = Clock.System.now()
                refreshTracker.notifyRefreshed(now)
                updateViewState {
                    // Yield/dividends are only meaningful once dividend info is resolved. Until then
                    // keep "--%" (first load) or the last resolved yield (later re-emissions), so the
                    // UI never flashes 0.00%. Decided from the same emission → no cross-coroutine race.
                    val resolved = converted.isDividendDataResolved
                    val previous = this.summary?.takeIf { !yieldLoading }
                    val shown = if (!resolved && previous != null) {
                        converted.copy(totalYield = previous.totalYield)
                    } else {
                        converted
                    }
                    copy(
                        summary = shown,
                        convertedSummary = shown,
                        yieldLoading = yieldLoading && !resolved,
                        currency = currency,
                        totalGainPercent = converted.totalGainPercent,
                        totalGainAbsolute = converted.totalGain,
                        summaryLoading = false,
                        isRefreshing = false,
                        lastUpdated = now,
                        error = null,
                    )
                }
            }
    }

    private suspend fun observePeriodGain(
        portfolioShared: kotlinx.coroutines.flow.Flow<List<SecurityHolding>>,
        currencyFlow: kotlinx.coroutines.flow.Flow<Currency>,
    ) {
        combine(portfolioShared, periodFlow, currencyFlow) { holdings, period, currency ->
            Triple(holdings, period, currency)
        }.collectLatest { (holdings, period, currency) ->
            // Computed directly in the display currency; null = rate unavailable → keep last value.
            val (abs, pct) = getPortfolioPeriodGain(holdings, period.toMarketPeriod(), currency)
                ?: return@collectLatest
            updateViewState {
                copy(
                    periodGainAbsolute = abs,
                    periodGainPercent = pct,
                    periodGainLoading = false,
                )
            }
        }
    }

    private suspend fun observePortfolioToday(
        portfolioShared: kotlinx.coroutines.flow.Flow<List<SecurityHolding>>,
        currencyFlow: kotlinx.coroutines.flow.Flow<Currency>,
    ) {
        combine(portfolioShared, currencyFlow) { holdings, currency -> holdings to currency }
            .collect { (holdings, currency) ->
                val gainers = holdings.filter { it.quote.changePercent > 0 }
                    .sortedByDescending { it.quote.changePercent }
                    .take(TOP_MOVERS_COUNT)
                val losers = holdings.filter { it.quote.changePercent < 0 }
                    .sortedBy { it.quote.changePercent }
                    .take(TOP_MOVERS_COUNT)
                val gainersItems = gainers.map { it.toItem(currency) }
                val losersItems = losers.map { it.toItem(currency) }
                updateViewState {
                    copy(
                        topGainers = gainersItems,
                        topLosers = losersItems,
                        portfolioTodayLoading = false,
                    )
                }
            }
    }

    private suspend fun SecurityHolding.toItem(currency: Currency): PortfolioTodayItem {
        // quote.price is in the security's own currency, not USD.
        val quoteCurrency = Currency.entries.firstOrNull { it.code == quote.currency } ?: Currency.USD
        val price = currencyConverter.convert(quote.price, quoteCurrency, currency).getOrElse { quote.price }
        return PortfolioTodayItem(
            ticker = holding.tickerId,
            name = quote.name,
            changePercent = quote.changePercent,
            price = price,
            currency = currency,
        )
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
                    observeMarketIndices()
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

    private fun loadMarketNews() {
        viewModelScope.launch {
            updateViewState { copy(marketNewsLoading = true) }
            settingsFlow
                .filterNotNull()
                .map { it.defaultMarket }
                .distinctUntilChanged()
                .collect { market ->
                    getMarketNews(market, count = NEWS_COUNT).onSuccess { news ->
                        val newsUi = news.map { item ->
                            NewsItemUi(
                                title = item.title,
                                publisher = item.publisher,
                                link = item.link,
                                publishedAtEpochSeconds = item.publishedAt.epochSeconds,
                                summary = item.summary,
                            )
                        }
                        updateViewState { copy(marketNews = newsUi, marketNewsLoading = false) }
                    }.onFailure {
                        updateViewState { copy(marketNewsLoading = false) }
                    }
                }
        }
    }

    private fun observeMarketIndices() {
        marketIndicesJob?.cancel()
        updateViewState { copy(marketIndicesLoading = true, marketIndicesError = false) }
        marketIndicesJob = viewModelScope.launch {
            runCatching {
                settingsFlow
                    .filterNotNull()
                    .map { it.defaultMarket }
                    .distinctUntilChanged()
                    .collect { market -> loadMarketIndices(market) }
            }.onFailure {
                updateViewState { copy(marketIndicesError = true, marketIndicesLoading = false) }
            }
        }
    }

    private suspend fun loadMarketIndices(defaultMarket: String) {
        updateViewState { copy(marketIndicesLoading = true, marketIndicesError = false) }
        getMajorMarketIndices(defaultMarket).fold(
            onSuccess = { indices ->
                updateViewState { copy(marketIndices = indices, marketIndicesLoading = false) }
            },
            onFailure = {
                updateViewState { copy(marketIndicesError = true, marketIndicesLoading = false) }
            },
        )
    }

    private companion object {
        const val SYNC_MAX_WAIT_MS = 5_000L
        const val TOP_MOVERS_COUNT = 3
        const val NEWS_COUNT = 5
    }
}
