package com.akole.dividox.feature.dashboard

import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.component.market.domain.usecase.GetCompanyInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.model.Currency
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import com.akole.dividox.component.watchlist.domain.repository.WatchlistRepository
import com.akole.dividox.component.watchlist.domain.usecase.GetWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardSideEffect
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent
import com.akole.dividox.integration.security.domain.usecase.GetEnrichedWatchlistUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioSummaryUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import com.akole.dividox.component.market.domain.model.ChartPeriod as MarketChartPeriod

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun teardown() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    // ─── Factory helpers ─────────────────────────────────────────────────────

    private fun viewModel(
        portfolioRepository: FakePortfolioRepository = FakePortfolioRepository(),
        marketRepository: FakeMarketRepository = FakeMarketRepository(),
        watchlistRepository: FakeWatchlistRepository = FakeWatchlistRepository(),
    ): DashboardViewModel {
        val getPortfolioUseCase = GetPortfolioUseCase(portfolioRepository)
        val getMultipleQuotesUseCase = GetMultipleQuotesUseCase(marketRepository)
        val getDividendInfoUseCase = GetDividendInfoUseCase(marketRepository)
        val getCompanyInfoUseCase = GetCompanyInfoUseCase(marketRepository)
        val getWatchlistUseCase = GetWatchlistUseCase(watchlistRepository)
        val getPortfolioWithQuotesUseCase = GetPortfolioWithQuotesUseCase(
            getPortfolioUseCase = getPortfolioUseCase,
            getMultipleQuotesUseCase = getMultipleQuotesUseCase,
            getDividendInfoUseCase = getDividendInfoUseCase,
        )
        return DashboardViewModel(
            getPortfolioSummary = GetPortfolioSummaryUseCase(getPortfolioWithQuotesUseCase),
            getEnrichedWatchlist = GetEnrichedWatchlistUseCase(
                getWatchlistUseCase = getWatchlistUseCase,
                getMultipleQuotesUseCase = getMultipleQuotesUseCase,
                getCompanyInfoUseCase = getCompanyInfoUseCase,
                getPortfolioUseCase = getPortfolioUseCase,
            ),
            removeFromWatchlist = RemoveFromWatchlistUseCase(watchlistRepository),
        )
    }

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `SHOULD set isLoading false WHEN data emits GIVEN initial load with holdings`() = runTest {
        val portfolio = FakePortfolioRepository()
        val market = FakeMarketRepository()
        portfolio.setHolding("AAPL", shares = 10.0, purchasePrice = 100.0)
        market.setQuote("AAPL", price = 150.0)
        val vm = viewModel(portfolioRepository = portfolio, marketRepository = market)
        advanceUntilIdle()
        assertFalse(vm.viewState.value.isLoading)
    }

    @Test
    fun `SHOULD set isLoading false WHEN data emits GIVEN initial load with empty portfolio`() = runTest {
        val vm = viewModel()
        advanceUntilIdle()
        assertFalse(vm.viewState.value.isLoading)
    }

    @Test
    fun `SHOULD populate summary WHEN portfolio has holdings GIVEN initial load`() = runTest {
        val portfolio = FakePortfolioRepository()
        val market = FakeMarketRepository()
        portfolio.setHolding("AAPL", shares = 10.0, purchasePrice = 100.0)
        market.setQuote("AAPL", price = 150.0)
        val vm = viewModel(portfolioRepository = portfolio, marketRepository = market)
        advanceUntilIdle()
        assertNotNull(vm.viewState.value.summary)
        assertEquals(1500.0, vm.viewState.value.summary?.totalValue)
    }

    @Test
    fun `SHOULD populate watchlist WHEN watchlist has entries GIVEN initial load`() = runTest {
        val watchlistRepo = FakeWatchlistRepository()
        val market = FakeMarketRepository()
        watchlistRepo.add("MSFT")
        market.setQuote("MSFT", price = 400.0)
        val vm = viewModel(watchlistRepository = watchlistRepo, marketRepository = market)
        advanceUntilIdle()
        assertEquals(1, vm.viewState.value.watchlist.size)
        assertEquals("MSFT", vm.viewState.value.watchlist.first().entry.tickerId)
    }

    // ─── PeriodSelected ───────────────────────────────────────────────────────

    @Test
    fun `SHOULD default to ONE_MONTH WHEN created GIVEN no events`() {
        val vm = viewModel()
        assertEquals(ChartPeriod.ONE_MONTH, vm.viewState.value.selectedPeriod)
    }

    @Test
    fun `SHOULD update selectedPeriod WHEN PeriodSelected event GIVEN ONE_YEAR period`() {
        val vm = viewModel()
        vm.onViewEvent(DashboardViewEvent.PeriodSelected(ChartPeriod.ONE_YEAR))
        assertEquals(ChartPeriod.ONE_YEAR, vm.viewState.value.selectedPeriod)
    }

    @Test
    fun `SHOULD update selectedPeriod WHEN PeriodSelected event GIVEN YTD period`() {
        val vm = viewModel()
        vm.onViewEvent(DashboardViewEvent.PeriodSelected(ChartPeriod.YEAR_TO_DATE))
        assertEquals(ChartPeriod.YEAR_TO_DATE, vm.viewState.value.selectedPeriod)
    }

    // ─── CurrencyToggled ──────────────────────────────────────────────────────

    @Test
    fun `SHOULD set showInEur to true WHEN CurrencyToggled GIVEN showInEur was false`() {
        val vm = viewModel()
        assertFalse(vm.viewState.value.showInEur)
        vm.onViewEvent(DashboardViewEvent.CurrencyToggled)
        assertTrue(vm.viewState.value.showInEur)
    }

    @Test
    fun `SHOULD set showInEur back to false WHEN CurrencyToggled twice GIVEN initial state`() {
        val vm = viewModel()
        vm.onViewEvent(DashboardViewEvent.CurrencyToggled)
        vm.onViewEvent(DashboardViewEvent.CurrencyToggled)
        assertFalse(vm.viewState.value.showInEur)
    }

    // ─── FavouriteToggled ─────────────────────────────────────────────────────

    @Test
    fun `SHOULD remove ticker from watchlist WHEN FavouriteToggled GIVEN ticker in watchlist`() = runTest {
        val watchlistRepo = FakeWatchlistRepository()
        watchlistRepo.add("AAPL")
        val vm = viewModel(watchlistRepository = watchlistRepo)
        vm.onViewEvent(DashboardViewEvent.FavouriteToggled("AAPL"))
        advanceUntilIdle()
        assertTrue(watchlistRepo.removedTickers.contains("AAPL"))
    }

    // ─── SecurityClicked ──────────────────────────────────────────────────────

    @Test
    fun `SHOULD emit NavigateToSecurity WHEN SecurityClicked GIVEN ticker`() = runTest {
        val vm = viewModel()
        val effects = mutableListOf<DashboardSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        vm.onViewEvent(DashboardViewEvent.SecurityClicked("MSFT"))
        advanceUntilIdle()
        job.cancel()

        val effect = assertIs<DashboardSideEffect.Navigation.NavigateToSecurity>(effects.first())
        assertEquals("MSFT", effect.ticker)
    }

    // ─── ViewAllFavouritesClicked ─────────────────────────────────────────────

    @Test
    fun `SHOULD emit NavigateToFavorites WHEN ViewAllFavouritesClicked GIVEN any state`() = runTest {
        val vm = viewModel()
        val effects = mutableListOf<DashboardSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        vm.onViewEvent(DashboardViewEvent.ViewAllFavouritesClicked)
        advanceUntilIdle()
        job.cancel()

        assertIs<DashboardSideEffect.Navigation.NavigateToFavorites>(effects.first())
    }
}

// ─── Fake Repositories ────────────────────────────────────────────────────────

private class FakePortfolioRepository : PortfolioRepository {
    private val holdingsFlow = MutableStateFlow<Result<List<Holding>>>(Result.success(emptyList()))

    fun setHolding(tickerId: String, shares: Double, purchasePrice: Double) {
        holdingsFlow.value = Result.success(
            listOf(
                Holding(
                    id = HoldingId("h1"),
                    tickerId = tickerId,
                    shares = shares,
                    purchasePrice = purchasePrice,
                    purchaseCurrency = Currency.USD,
                    purchaseDate = 0L,
                ),
            ),
        )
    }

    override fun observePortfolio(): Flow<Result<List<Holding>>> = holdingsFlow
    override suspend fun getPortfolio(): Result<List<Holding>> = holdingsFlow.value
    override suspend fun addHolding(holding: Holding): Result<HoldingId> = Result.success(holding.id)
    override suspend fun updateHolding(holding: Holding): Result<Unit> = Result.success(Unit)
    override suspend fun removeHolding(holdingId: HoldingId): Result<Unit> = Result.success(Unit)
}

private class FakeMarketRepository : MarketRepository {
    private val quotes = mutableMapOf<String, Double>()

    fun setQuote(ticker: String, price: Double) {
        quotes[ticker] = price
    }

    override suspend fun getStockQuote(ticker: String): Result<StockQuote> =
        quotes[ticker]?.let { price ->
            Result.success(
                StockQuote(
                    ticker = ticker,
                    price = price,
                    change = 0.0,
                    changePercent = 0.0,
                    currency = "USD",
                    lastUpdated = Instant.fromEpochMilliseconds(0),
                ),
            )
        } ?: Result.failure(IllegalStateException("No quote for $ticker"))

    override suspend fun getMultipleQuotes(tickers: List<String>): Result<List<StockQuote>> =
        Result.success(
            tickers.mapNotNull { ticker ->
                quotes[ticker]?.let { price ->
                    StockQuote(
                        ticker = ticker,
                        price = price,
                        change = 0.0,
                        changePercent = 0.0,
                        currency = "USD",
                        lastUpdated = Instant.fromEpochMilliseconds(0),
                    )
                }
            },
        )

    override suspend fun getDividendInfo(ticker: String): Result<DividendInfo> =
        Result.failure(IllegalStateException("Not set"))

    override suspend fun getCompanyInfo(ticker: String): Result<CompanyInfo> =
        Result.failure(IllegalStateException("Not set"))

    override suspend fun getDividendHistory(ticker: String): Result<List<DividendInfo>> =
        Result.success(emptyList())

    override fun getPriceHistory(ticker: String, period: MarketChartPeriod): Flow<List<PricePoint>> =
        flowOf(emptyList())

    override suspend fun searchSecurities(query: String): Result<List<StockQuote>> =
        Result.success(emptyList())
}

private class FakeWatchlistRepository : WatchlistRepository {
    val removedTickers = mutableListOf<String>()
    private val entries = MutableStateFlow<List<WatchlistEntry>>(emptyList())

    fun add(tickerId: String) {
        entries.value = entries.value + WatchlistEntry(
            tickerId = tickerId,
            addedAt = Clock.System.now(),
        )
    }

    override fun getWatchlist(): Flow<List<WatchlistEntry>> = entries
    override suspend fun addToWatchlist(tickerId: String) {
        add(tickerId)
    }
    override suspend fun removeFromWatchlist(tickerId: String) {
        removedTickers += tickerId
        entries.value = entries.value.filterNot { it.tickerId == tickerId }
    }
    override fun isInWatchlist(tickerId: String): Flow<Boolean> =
        entries.map { list -> list.any { it.tickerId == tickerId } }
}
