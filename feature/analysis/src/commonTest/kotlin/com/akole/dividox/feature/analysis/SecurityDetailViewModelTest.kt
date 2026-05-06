package com.akole.dividox.feature.analysis

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.AppRefreshTracker
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.usecase.GetHistoricalDividendEventsUseCase
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.watchlist.domain.usecase.AddToWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.IsInWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailSideEffect
import com.akole.dividox.feature.analysis.SecurityDetailContract.SecurityDetailViewEvent
import com.akole.dividox.integration.security.domain.model.SecurityDetail
import com.akole.dividox.integration.security.domain.usecase.GetSecurityDetailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SecurityDetailViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = UnconfinedTestDispatcher(testScheduler)

    private val ticker = "AAPL"

    private val mockGetSecurityDetail = mockk<GetSecurityDetailUseCase>()
    private val mockGetStockQuote = mockk<GetStockQuoteUseCase>()
    private val mockGetHistoricalDividendEvents = mockk<GetHistoricalDividendEventsUseCase>()
    private val mockGetPriceHistory = mockk<GetPriceHistoryUseCase>()
    private val mockIsInWatchlist = mockk<IsInWatchlistUseCase>()
    private val mockAddToWatchlist = mockk<AddToWatchlistUseCase>()
    private val mockRemoveFromWatchlist = mockk<RemoveFromWatchlistUseCase>()
    private val mockConnectivityManager = mockk<NetworkConnectivityManager>()
    private val mockObserveAppSettings = mockk<ObserveAppSettingsUseCase>()
    private val mockCurrencyConverter = mockk<CurrencyConverter>()
    private val mockRefreshTracker = mockk<AppRefreshTracker>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { mockObserveAppSettings() } returns flowOf(AppSettings(currency = Currency.USD))
        every { mockIsInWatchlist(any()) } returns flowOf(false)
        every { mockConnectivityManager.observeConnectivity() } returns emptyFlow()
        every { mockGetSecurityDetail(any(), any()) } returns emptyFlow()
        every { mockGetPriceHistory(any(), any()) } returns flowOf(emptyList())
        coEvery { mockGetHistoricalDividendEvents(any(), any()) } returns Result.success(emptyList())
        coEvery { mockAddToWatchlist(any()) } just Runs
        coEvery { mockRemoveFromWatchlist(any()) } just Runs
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SecurityDetailViewModel(
        ticker = ticker,
        getSecurityDetail = mockGetSecurityDetail,
        getStockQuote = mockGetStockQuote,
        getHistoricalDividendEvents = mockGetHistoricalDividendEvents,
        getPriceHistory = mockGetPriceHistory,
        isInWatchlist = mockIsInWatchlist,
        addToWatchlist = mockAddToWatchlist,
        removeFromWatchlist = mockRemoveFromWatchlist,
        connectivityManager = mockConnectivityManager,
        observeAppSettings = mockObserveAppSettings,
        currencyConverter = mockCurrencyConverter,
        refreshTracker = mockRefreshTracker,
    )

    // ─── Load ─────────────────────────────────────────────────────────────────

    @Test
    fun `SHOULD populate state WHEN security detail emits GIVEN OnLoad`() = runTest(testScheduler) {
        // GIVEN
        val detail = aSecurityDetail()
        every { mockGetSecurityDetail(ticker, ChartPeriod.ONE_YEAR) } returns flowOf(detail)
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SecurityDetailViewEvent.OnLoad)
        advanceUntilIdle()

        // THEN
        assertEquals(ticker, vm.viewState.value.ticker)
        assertEquals(detail.quote.price, vm.viewState.value.quote?.price)
    }

    // ─── Watchlist ────────────────────────────────────────────────────────────

    @Test
    fun `SHOULD call addToWatchlist WHEN OnFavoriteToggled GIVEN not in watchlist`() = runTest(testScheduler) {
        // GIVEN
        every { mockIsInWatchlist(any()) } returns flowOf(false)
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(SecurityDetailViewEvent.OnFavoriteToggled)
        advanceUntilIdle()

        // THEN
        coVerify { mockAddToWatchlist(ticker) }
    }

    // ─── Chart period ─────────────────────────────────────────────────────────

    @Test
    fun `SHOULD update selectedChartPeriod WHEN ChartPeriodSelected GIVEN new period`() = runTest(testScheduler) {
        // GIVEN
        every { mockGetSecurityDetail(ticker, ChartPeriod.ONE_WEEK) } returns emptyFlow()
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SecurityDetailViewEvent.ChartPeriodSelected(ChartPeriod.ONE_WEEK))
        advanceUntilIdle()

        // THEN
        assertEquals(ChartPeriod.ONE_WEEK, vm.viewState.value.selectedChartPeriod)
    }

    // ─── Dividend chart toggle ────────────────────────────────────────────────

    @Test
    fun `SHOULD toggle isDividendChartPercentage WHEN ToggleDividendChartMode`() = runTest(testScheduler) {
        // GIVEN
        val vm = viewModel()
        val initial = vm.viewState.value.isDividendChartPercentage

        // WHEN
        vm.onViewEvent(SecurityDetailViewEvent.ToggleDividendChartMode)

        // THEN
        assertEquals(!initial, vm.viewState.value.isDividendChartPercentage)
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @Test
    fun `SHOULD emit NavigateBack WHEN OnBackClicked`() = runTest(testScheduler) {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<SecurityDetailSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(SecurityDetailViewEvent.OnBackClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        assertIs<SecurityDetailSideEffect.Navigation.NavigateBack>(effects.first())
    }

    @Test
    fun `SHOULD emit NavigateToEditHolding WHEN OnEditHoldingClicked GIVEN ticker in portfolio`() = runTest(testScheduler) {
        // GIVEN
        val holdingId = HoldingId("holding-1")
        every { mockGetSecurityDetail(any(), any()) } returns flowOf(
            aSecurityDetail(isInPortfolio = true, holdingId = holdingId)
        )
        val vm = viewModel()
        advanceUntilIdle()
        val effects = mutableListOf<SecurityDetailSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(SecurityDetailViewEvent.OnEditHoldingClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        val effect = assertIs<SecurityDetailSideEffect.Navigation.NavigateToEditHolding>(effects.first())
        assertEquals(holdingId, effect.holdingId)
    }

    @Test
    fun `SHOULD emit NavigateToAddSecurity WHEN OnAddSecurityClicked GIVEN ticker not in portfolio`() = runTest(testScheduler) {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<SecurityDetailSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(SecurityDetailViewEvent.OnAddSecurityClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        val effect = assertIs<SecurityDetailSideEffect.Navigation.NavigateToAddSecurity>(effects.first())
        assertEquals(ticker, effect.ticker)
    }

    @Test
    fun `SHOULD NOT emit NavigateToEditHolding WHEN OnEditHoldingClicked GIVEN holdingId is null`() = runTest(testScheduler) {
        // GIVEN
        every { mockGetSecurityDetail(any(), any()) } returns flowOf(
            aSecurityDetail(isInPortfolio = true, holdingId = null)
        )
        val vm = viewModel()
        advanceUntilIdle()
        val effects = mutableListOf<SecurityDetailSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(SecurityDetailViewEvent.OnEditHoldingClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        assertTrue(effects.isEmpty())
    }

    // ─── Test fixtures ────────────────────────────────────────────────────────

    private fun aSecurityDetail(
        isInPortfolio: Boolean = false,
        holdingId: HoldingId? = null,
    ) = SecurityDetail(
        ticker = ticker,
        quote = StockQuote(
            ticker = ticker,
            price = 150.0,
            change = 0.0,
            changePercent = 0.0,
            currency = "USD",
            lastUpdated = Instant.fromEpochMilliseconds(0),
        ),
        dividendInfo = null,
        companyInfo = null,
        priceHistory = emptyList(),
        isInPortfolio = isInPortfolio,
        isInWatchlist = false,
        holdingId = holdingId,
    )
}
