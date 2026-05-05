package com.akole.dividox.feature.favorites

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesSideEffect
import com.akole.dividox.feature.favorites.FavoritesContract.FavoritesViewEvent
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry
import com.akole.dividox.integration.security.domain.usecase.GetEnrichedWatchlistUseCase
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
class FavoritesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getEnrichedWatchlist: GetEnrichedWatchlistUseCase = mockk()
    private val removeFromWatchlist: RemoveFromWatchlistUseCase = mockk()
    private val observeAppSettings: ObserveAppSettingsUseCase = mockk()
    private val currencyConverter: CurrencyConverter = mockk()
    private val connectivityManager: NetworkConnectivityManager = mockk()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getEnrichedWatchlist() } returns emptyFlow()
        every { observeAppSettings() } returns flowOf(AppSettings())
        every { connectivityManager.observeConnectivity() } returns emptyFlow()
        coEvery { currencyConverter.convert(any(), any(), any()) } answers { Result.success(firstArg()) }
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = FavoritesViewModel(
        getEnrichedWatchlist = getEnrichedWatchlist,
        removeFromWatchlist = removeFromWatchlist,
        observeAppSettings = observeAppSettings,
        currencyConverter = currencyConverter,
        connectivityManager = connectivityManager,
    )

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `SHOULD populate favorites WHEN watchlist emits entries GIVEN initial load`() = runTest {
        // GIVEN
        every { getEnrichedWatchlist() } returns flowOf(listOf(anEntry("AAPL"), anEntry("MSFT")))

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertEquals(2, vm.viewState.value.favorites.size)
        assertEquals("AAPL", vm.viewState.value.favorites[0].entry.tickerId)
        assertEquals("MSFT", vm.viewState.value.favorites[1].entry.tickerId)
    }

    @Test
    fun `SHOULD set isLoading false WHEN watchlist emits GIVEN initial load`() = runTest {
        // GIVEN
        every { getEnrichedWatchlist() } returns flowOf(emptyList())

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertEquals(false, vm.viewState.value.isLoading)
    }

    @Test
    fun `SHOULD reflect currency from settings WHEN settings emit EUR GIVEN initial state`() = runTest {
        // GIVEN
        every { getEnrichedWatchlist() } returns flowOf(emptyList())
        every { observeAppSettings() } returns flowOf(AppSettings(currency = Currency.EUR))

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertEquals(Currency.EUR, vm.viewState.value.currency)
    }

    // ─── Search filtering ─────────────────────────────────────────────────────

    @Test
    fun `SHOULD filter by ticker WHEN search query matches ticker GIVEN entries loaded`() = runTest {
        // GIVEN
        every { getEnrichedWatchlist() } returns flowOf(listOf(anEntry("AAPL"), anEntry("MSFT")))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(FavoritesViewEvent.SearchQueryChanged("AAPL"))
        advanceUntilIdle()

        // THEN
        assertEquals(1, vm.viewState.value.favorites.size)
        assertEquals("AAPL", vm.viewState.value.favorites.first().entry.tickerId)
    }

    @Test
    fun `SHOULD filter case-insensitive WHEN search query is lowercase GIVEN entries loaded`() = runTest {
        // GIVEN
        every { getEnrichedWatchlist() } returns flowOf(listOf(anEntry("AAPL"), anEntry("MSFT")))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(FavoritesViewEvent.SearchQueryChanged("msft"))
        advanceUntilIdle()

        // THEN
        assertEquals(1, vm.viewState.value.favorites.size)
        assertEquals("MSFT", vm.viewState.value.favorites.first().entry.tickerId)
    }

    @Test
    fun `SHOULD filter by company name WHEN search query matches name GIVEN entries with company info`() = runTest {
        // GIVEN
        val apple = anEntry("AAPL", companyName = "Apple Inc")
        val microsoft = anEntry("MSFT", companyName = "Microsoft Corporation")
        every { getEnrichedWatchlist() } returns flowOf(listOf(apple, microsoft))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(FavoritesViewEvent.SearchQueryChanged("apple"))
        advanceUntilIdle()

        // THEN
        assertEquals(1, vm.viewState.value.favorites.size)
        assertEquals("AAPL", vm.viewState.value.favorites.first().entry.tickerId)
    }

    @Test
    fun `SHOULD show all entries WHEN search query is cleared GIVEN previously filtered`() = runTest {
        // GIVEN
        every { getEnrichedWatchlist() } returns flowOf(listOf(anEntry("AAPL"), anEntry("MSFT")))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onViewEvent(FavoritesViewEvent.SearchQueryChanged("AAPL"))
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(FavoritesViewEvent.SearchQueryChanged(""))
        advanceUntilIdle()

        // THEN
        assertEquals(2, vm.viewState.value.favorites.size)
    }

    // ─── FavoriteToggled ──────────────────────────────────────────────────────

    @Test
    fun `SHOULD call removeFromWatchlist WHEN FavoriteToggled GIVEN ticker`() = runTest {
        // GIVEN
        coEvery { removeFromWatchlist("AAPL") } just Runs
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(FavoritesViewEvent.FavoriteToggled("AAPL"))
        advanceUntilIdle()

        // THEN
        coVerify { removeFromWatchlist("AAPL") }
    }

    // ─── SecurityClicked ──────────────────────────────────────────────────────

    @Test
    fun `SHOULD emit NavigateToSecurity WHEN SecurityClicked GIVEN ticker`() = runTest {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<FavoritesSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(FavoritesViewEvent.SecurityClicked("MSFT"))
        advanceUntilIdle()
        job.cancel()

        // THEN
        val effect = assertIs<FavoritesSideEffect.Navigation.NavigateToSecurity>(effects.first())
        assertEquals("MSFT", effect.ticker)
    }

    // ─── BackClicked ──────────────────────────────────────────────────────────

    @Test
    fun `SHOULD emit NavigateBack WHEN BackClicked GIVEN any state`() = runTest {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<FavoritesSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(FavoritesViewEvent.BackClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        assertIs<FavoritesSideEffect.Navigation.NavigateBack>(effects.first())
    }

    // ─── Test fixtures ────────────────────────────────────────────────────────

    private fun anEntry(ticker: String, companyName: String? = null) = EnrichedWatchlistEntry(
        entry = WatchlistEntry(tickerId = ticker, addedAt = Instant.fromEpochMilliseconds(0)),
        quote = null,
        companyInfo = companyName?.let { CompanyInfo(ticker = ticker, name = it, exchange = "", logoUrl = null) },
        isInPortfolio = false,
    )
}
