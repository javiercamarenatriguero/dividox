package com.akole.dividox.feature.dashboard

import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.settings.domain.usecase.SetCurrencyUseCase
import com.akole.dividox.common.ui.resources.Currency
import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardSideEffect
import com.akole.dividox.feature.dashboard.DashboardContract.DashboardViewEvent
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry
import com.akole.dividox.integration.security.domain.model.PortfolioSummary
import com.akole.dividox.integration.security.domain.usecase.GetEnrichedWatchlistUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioSummaryUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
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
import kotlin.test.assertNotNull
import kotlin.test.assertFalse
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getPortfolioSummary: GetPortfolioSummaryUseCase = mockk()
    private val getEnrichedWatchlist: GetEnrichedWatchlistUseCase = mockk()
    private val removeFromWatchlist: RemoveFromWatchlistUseCase = mockk()
    private val observeAppSettings: ObserveAppSettingsUseCase = mockk()
    private val setCurrency: SetCurrencyUseCase = mockk()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getPortfolioSummary() } returns emptyFlow()
        every { getEnrichedWatchlist() } returns emptyFlow()
        every { observeAppSettings() } returns flowOf(AppSettings())
        coEvery { setCurrency(any()) } just Runs
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = DashboardViewModel(
        getPortfolioSummary = getPortfolioSummary,
        getEnrichedWatchlist = getEnrichedWatchlist,
        removeFromWatchlist = removeFromWatchlist,
        observeAppSettings = observeAppSettings,
        setCurrency = setCurrency,
    )

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `SHOULD set isLoading false WHEN data emits GIVEN initial load with holdings`() = runTest {
        // GIVEN
        every { getPortfolioSummary() } returns flowOf(aSummary)
        every { getEnrichedWatchlist() } returns flowOf(emptyList())

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertFalse(vm.viewState.value.isLoading)
    }

    @Test
    fun `SHOULD set isLoading false WHEN data emits GIVEN empty portfolio`() = runTest {
        // GIVEN
        every { getPortfolioSummary() } returns flowOf(emptySummary)
        every { getEnrichedWatchlist() } returns flowOf(emptyList())

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertFalse(vm.viewState.value.isLoading)
    }

    @Test
    fun `SHOULD populate summary WHEN portfolio has holdings GIVEN initial load`() = runTest {
        // GIVEN
        every { getPortfolioSummary() } returns flowOf(aSummary)
        every { getEnrichedWatchlist() } returns flowOf(emptyList())

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertNotNull(vm.viewState.value.summary)
        assertEquals(1500.0, vm.viewState.value.summary?.totalValue)
    }

    @Test
    fun `SHOULD populate watchlist WHEN watchlist has entries GIVEN initial load`() = runTest {
        // GIVEN
        val entry = anEntry("MSFT")
        every { getPortfolioSummary() } returns flowOf(emptySummary)
        every { getEnrichedWatchlist() } returns flowOf(listOf(entry))

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertEquals(1, vm.viewState.value.watchlist.size)
        assertEquals("MSFT", vm.viewState.value.watchlist.first().entry.tickerId)
    }

    // ─── Settings observation ─────────────────────────────────────────────────

    @Test
    fun `SHOULD reflect EUR currency WHEN settings emit EUR GIVEN initial state`() = runTest {
        // GIVEN
        every { observeAppSettings() } returns flowOf(AppSettings(currency = Currency.EUR))

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertEquals(Currency.EUR, vm.viewState.value.currency)
    }

    @Test
    fun `SHOULD reflect USD currency WHEN settings emit USD GIVEN persisted preference`() = runTest {
        // GIVEN
        every { observeAppSettings() } returns flowOf(AppSettings(currency = Currency.USD))

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertEquals(Currency.USD, vm.viewState.value.currency)
    }

    // ─── PeriodSelected ───────────────────────────────────────────────────────

    @Test
    fun `SHOULD default to ONE_MONTH WHEN created GIVEN no events`() {
        // GIVEN / WHEN
        val vm = viewModel()

        // THEN
        assertEquals(ChartPeriod.ONE_MONTH, vm.viewState.value.selectedPeriod)
    }

    @Test
    fun `SHOULD update selectedPeriod WHEN PeriodSelected GIVEN ONE_YEAR`() {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(DashboardViewEvent.PeriodSelected(ChartPeriod.ONE_YEAR))

        // THEN
        assertEquals(ChartPeriod.ONE_YEAR, vm.viewState.value.selectedPeriod)
    }

    @Test
    fun `SHOULD update selectedPeriod WHEN PeriodSelected GIVEN YTD`() {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(DashboardViewEvent.PeriodSelected(ChartPeriod.YEAR_TO_DATE))

        // THEN
        assertEquals(ChartPeriod.YEAR_TO_DATE, vm.viewState.value.selectedPeriod)
    }

    // ─── CurrencyToggled ──────────────────────────────────────────────────────

    @Test
    fun `SHOULD call setCurrency with USD WHEN CurrencyToggled GIVEN current currency is EUR`() = runTest {
        // GIVEN
        every { observeAppSettings() } returns flowOf(AppSettings(currency = Currency.EUR))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(DashboardViewEvent.CurrencyToggled)
        advanceUntilIdle()

        // THEN
        coVerify { setCurrency(Currency.USD) }
    }

    @Test
    fun `SHOULD call setCurrency with EUR WHEN CurrencyToggled GIVEN current currency is USD`() = runTest {
        // GIVEN
        every { observeAppSettings() } returns flowOf(AppSettings(currency = Currency.USD))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(DashboardViewEvent.CurrencyToggled)
        advanceUntilIdle()

        // THEN
        coVerify { setCurrency(Currency.EUR) }
    }

    // ─── FavouriteToggled ─────────────────────────────────────────────────────

    @Test
    fun `SHOULD call removeFromWatchlist WHEN FavouriteToggled GIVEN ticker`() = runTest {
        // GIVEN
        coEvery { removeFromWatchlist("AAPL") } just Runs
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(DashboardViewEvent.FavouriteToggled("AAPL"))
        advanceUntilIdle()

        // THEN
        coVerify { removeFromWatchlist("AAPL") }
    }

    // ─── SecurityClicked ──────────────────────────────────────────────────────

    @Test
    fun `SHOULD emit NavigateToSecurity WHEN SecurityClicked GIVEN ticker`() = runTest {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<DashboardSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(DashboardViewEvent.SecurityClicked("MSFT"))
        advanceUntilIdle()
        job.cancel()

        // THEN
        val effect = assertIs<DashboardSideEffect.Navigation.NavigateToSecurity>(effects.first())
        assertEquals("MSFT", effect.ticker)
    }

    // ─── ViewAllFavouritesClicked ─────────────────────────────────────────────

    @Test
    fun `SHOULD emit NavigateToFavorites WHEN ViewAllFavouritesClicked GIVEN any state`() = runTest {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<DashboardSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(DashboardViewEvent.ViewAllFavouritesClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        assertIs<DashboardSideEffect.Navigation.NavigateToFavorites>(effects.first())
    }

    // ─── Test fixtures ────────────────────────────────────────────────────────

    private val aSummary = PortfolioSummary(
        totalValue = 1500.0,
        totalGain = 500.0,
        totalGainPercent = 50.0,
        totalYield = 2.5,
        dividendsCollected = 100.0,
    )

    private val emptySummary = PortfolioSummary(
        totalValue = 0.0,
        totalGain = 0.0,
        totalGainPercent = 0.0,
        totalYield = 0.0,
        dividendsCollected = 0.0,
    )

    private fun anEntry(ticker: String) = EnrichedWatchlistEntry(
        entry = WatchlistEntry(tickerId = ticker, addedAt = Instant.fromEpochMilliseconds(0)),
        quote = null,
        companyInfo = null,
        isInPortfolio = false,
    )
}
