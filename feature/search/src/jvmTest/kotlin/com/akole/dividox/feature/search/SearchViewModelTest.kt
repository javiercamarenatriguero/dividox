package com.akole.dividox.feature.search

import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.usecase.SearchSecuritiesUseCase
import com.akole.dividox.component.watchlist.domain.model.WatchlistEntry
import com.akole.dividox.component.watchlist.domain.usecase.AddToWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.GetWatchlistUseCase
import com.akole.dividox.component.watchlist.domain.usecase.RemoveFromWatchlistUseCase
import com.akole.dividox.feature.search.SearchContract.SearchSideEffect
import com.akole.dividox.feature.search.SearchContract.SearchViewEvent
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
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = UnconfinedTestDispatcher(testScheduler)

    private val searchSecurities: SearchSecuritiesUseCase = mockk()
    private val getWatchlist: GetWatchlistUseCase = mockk()
    private val addToWatchlist: AddToWatchlistUseCase = mockk()
    private val removeFromWatchlist: RemoveFromWatchlistUseCase = mockk()
    private val connectivityManager: NetworkConnectivityManager = mockk()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getWatchlist() } returns emptyFlow()
        every { connectivityManager.observeConnectivity() } returns emptyFlow()
        coEvery { searchSecurities(any()) } returns Result.success(emptyList())
        coEvery { addToWatchlist(any()) } just Runs
        coEvery { removeFromWatchlist(any()) } just Runs
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SearchViewModel(
        searchSecurities = searchSecurities,
        getWatchlist = getWatchlist,
        addToWatchlist = addToWatchlist,
        removeFromWatchlist = removeFromWatchlist,
        connectivityManager = connectivityManager,
    )

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    fun `SHOULD have empty results WHEN query is blank GIVEN initial state`() = runTest(testScheduler) {
        // GIVEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertTrue(vm.viewState.value.results.isEmpty())
        assertEquals(false, vm.viewState.value.isLoading)
    }

    // ─── Debounce ─────────────────────────────────────────────────────────────

    @Test
    fun `SHOULD NOT call searchSecurities WHEN less than 250ms passed GIVEN query entered`() = runTest(testScheduler) {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SearchViewEvent.QueryChanged("AAPL"))
        advanceTimeBy(100)

        // THEN
        coVerify(exactly = 0) { searchSecurities(any()) }
    }

    @Test
    fun `SHOULD call searchSecurities WHEN 250ms passed GIVEN non-blank query`() = runTest(testScheduler) {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SearchViewEvent.QueryChanged("AAPL"))
        advanceTimeBy(250)
        advanceUntilIdle()

        // THEN
        coVerify { searchSecurities("AAPL") }
    }

    @Test
    fun `SHOULD only call searchSecurities once WHEN multiple queries entered quickly GIVEN debounce`() = runTest(testScheduler) {
        // GIVEN
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SearchViewEvent.QueryChanged("A"))
        advanceTimeBy(100)
        vm.onViewEvent(SearchViewEvent.QueryChanged("AA"))
        advanceTimeBy(100)
        vm.onViewEvent(SearchViewEvent.QueryChanged("AAPL"))
        advanceTimeBy(250)
        advanceUntilIdle()

        // THEN
        coVerify(exactly = 1) { searchSecurities("AAPL") }
    }

    // ─── Results ──────────────────────────────────────────────────────────────

    @Test
    fun `SHOULD populate results WHEN searchSecurities returns quotes GIVEN query`() = runTest(testScheduler) {
        // GIVEN
        val quote = aQuote("AAPL")
        coEvery { searchSecurities("AAPL") } returns Result.success(listOf(quote))
        val vm = viewModel()

        // WHEN
        vm.onViewEvent(SearchViewEvent.QueryChanged("AAPL"))
        advanceTimeBy(250)
        advanceUntilIdle()

        // THEN
        assertEquals(1, vm.viewState.value.results.size)
        assertEquals("AAPL", vm.viewState.value.results.first().ticker)
    }

    @Test
    fun `SHOULD clear results WHEN query is cleared GIVEN previous results`() = runTest(testScheduler) {
        // GIVEN
        coEvery { searchSecurities("AAPL") } returns Result.success(listOf(aQuote("AAPL")))
        val vm = viewModel()
        vm.onViewEvent(SearchViewEvent.QueryChanged("AAPL"))
        advanceTimeBy(250)
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(SearchViewEvent.QueryChanged(""))
        advanceTimeBy(250)
        advanceUntilIdle()

        // THEN
        assertTrue(vm.viewState.value.results.isEmpty())
    }

    // ─── Watchlist ────────────────────────────────────────────────────────────

    @Test
    fun `SHOULD reflect watchlisted tickers WHEN watchlist emits entries GIVEN initial load`() = runTest(testScheduler) {
        // GIVEN
        every { getWatchlist() } returns flowOf(listOf(aWatchlistEntry("AAPL"), aWatchlistEntry("MSFT")))
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertEquals(setOf("AAPL", "MSFT"), vm.viewState.value.watchlistedTickers)
    }

    @Test
    fun `SHOULD call addToWatchlist WHEN FavouriteToggled on non-watchlisted ticker GIVEN state`() = runTest(testScheduler) {
        // GIVEN
        every { getWatchlist() } returns flowOf(emptyList())
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(SearchViewEvent.FavouriteToggled("AAPL"))
        advanceUntilIdle()

        // THEN
        coVerify { addToWatchlist("AAPL") }
        coVerify(exactly = 0) { removeFromWatchlist(any()) }
    }

    @Test
    fun `SHOULD call removeFromWatchlist WHEN FavouriteToggled on watchlisted ticker GIVEN state`() = runTest(testScheduler) {
        // GIVEN
        every { getWatchlist() } returns flowOf(listOf(aWatchlistEntry("AAPL")))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(SearchViewEvent.FavouriteToggled("AAPL"))
        advanceUntilIdle()

        // THEN
        coVerify { removeFromWatchlist("AAPL") }
        coVerify(exactly = 0) { addToWatchlist(any()) }
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @Test
    fun `SHOULD emit NavigateToSecurity WHEN SecurityClicked GIVEN ticker`() = runTest(testScheduler) {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<SearchSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(SearchViewEvent.SecurityClicked("MSFT"))
        advanceUntilIdle()
        job.cancel()

        // THEN
        val effect = assertIs<SearchSideEffect.Navigation.NavigateToSecurity>(effects.first())
        assertEquals("MSFT", effect.ticker)
    }

    @Test
    fun `SHOULD emit NavigateBack WHEN BackClicked GIVEN any state`() = runTest(testScheduler) {
        // GIVEN
        val vm = viewModel()
        val effects = mutableListOf<SearchSideEffect>()
        val job = launch { vm.sideEffect.collect(effects::add) }

        // WHEN
        vm.onViewEvent(SearchViewEvent.BackClicked)
        advanceUntilIdle()
        job.cancel()

        // THEN
        assertIs<SearchSideEffect.Navigation.NavigateBack>(effects.first())
    }

    // ─── Test fixtures ────────────────────────────────────────────────────────

    private fun aQuote(ticker: String) = StockQuote(
        ticker = ticker,
        price = 0.0,
        change = 0.0,
        changePercent = 0.0,
        currency = "USD",
        lastUpdated = Instant.fromEpochMilliseconds(0),
    )

    private fun aWatchlistEntry(ticker: String) = WatchlistEntry(
        tickerId = ticker,
        addedAt = Instant.fromEpochMilliseconds(0),
    )
}
