package com.akole.dividox.feature.portfolio

import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockUseCase = mockk<GetPortfolioWithQuotesUseCase>()
    private val mockObserveSettings = mockk<ObserveAppSettingsUseCase>()
    private val mockCurrencyConverter = mockk<CurrencyConverter>()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { mockUseCase() } returns emptyFlow()
        every { mockObserveSettings() } returns flowOf(AppSettings())
        coEvery { mockCurrencyConverter.convert(any(), any(), any()) } answers { Result.success(firstArg()) }
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PortfolioViewModel(mockUseCase, mockObserveSettings, mockCurrencyConverter)

    private fun createDividendInfo(ticker: String, yield: Double): DividendInfo {
        return DividendInfo(
            ticker = ticker,
            yield = yield,
            annualPayout = 100.0,
            payoutRatio = 0.3,
            fiveYearGrowth = 5.0,
            exDividendDate = null,
        )
    }

    private val apple = SecurityHolding(
        holding = Holding(
            id = HoldingId("h1"),
            tickerId = "AAPL",
            shares = 10.0,
            purchasePrice = 150.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 1705276800000L,
        ),
        quote = StockQuote(
            ticker = "AAPL",
            price = 180.0,
            change = 30.0,
            changePercent = 20.0,
            currency = "USD",
            lastUpdated = Instant.parse("2024-01-20T00:00:00Z"),
        ),
        dividendInfo = createDividendInfo("AAPL", 0.5),
        totalGainPercent = 20.0,
    )

    private val microsoft = SecurityHolding(
        holding = Holding(
            id = HoldingId("h2"),
            tickerId = "MSFT",
            shares = 5.0,
            purchasePrice = 300.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 1707868800000L,
        ),
        quote = StockQuote(
            ticker = "MSFT",
            price = 290.0,
            change = -10.0,
            changePercent = -3.33,
            currency = "USD",
            lastUpdated = Instant.parse("2024-01-20T00:00:00Z"),
        ),
        dividendInfo = createDividendInfo("MSFT", 0.8),
        totalGainPercent = -3.33,
    )

    // ─── Initialization ───────────────────────────────────────────────────

    @Test
    fun `SHOULD have loading true WHEN created GIVEN initial state`() = runTest {
        // GIVEN
        every { mockUseCase() } returns emptyFlow()

        // WHEN
        val vm = viewModel()

        // THEN
        assertTrue(vm.viewState.value.isLoading)
        assertEquals(emptyList(), vm.viewState.value.holdings)
        assertEquals("", vm.viewState.value.searchQuery)
        assertEquals(SortOrder(), vm.viewState.value.sortOrder)
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `SHOULD set loading false WHEN data emits GIVEN portfolio with holdings`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(apple, microsoft))

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertFalse(vm.viewState.value.isLoading)
        assertEquals(2, vm.viewState.value.holdings.size)
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `SHOULD set loading false WHEN data emits GIVEN empty portfolio`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(emptyList())

        // WHEN
        val vm = viewModel()
        advanceUntilIdle()

        // THEN
        assertFalse(vm.viewState.value.isLoading)
        assertEquals(emptyList(), vm.viewState.value.holdings)
        assertNull(vm.viewState.value.error)
    }

    // ─── Search Filtering ────────────────────────────────────────────────

    @Test
    fun `SHOULD filter by ticker WHEN search query changes GIVEN holdings loaded`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(apple, microsoft))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(PortfolioContract.PortfolioViewEvent.SearchQueryChanged("AAPL"))
        advanceUntilIdle()

        // THEN
        assertEquals("AAPL", vm.viewState.value.searchQuery)
        assertEquals(1, vm.viewState.value.holdings.size)
        assertEquals("AAPL", vm.viewState.value.holdings[0].holding.tickerId)
    }

    @Test
    fun `SHOULD filter case-insensitive WHEN search query is lowercase GIVEN holdings loaded`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(apple, microsoft))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(PortfolioContract.PortfolioViewEvent.SearchQueryChanged("aapl"))
        advanceUntilIdle()

        // THEN
        assertEquals(1, vm.viewState.value.holdings.size)
        assertEquals("AAPL", vm.viewState.value.holdings[0].holding.tickerId)
    }

    @Test
    fun `SHOULD return all holdings WHEN search cleared GIVEN previously filtered`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(apple, microsoft))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onViewEvent(PortfolioContract.PortfolioViewEvent.SearchQueryChanged("AAPL"))
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(PortfolioContract.PortfolioViewEvent.SearchQueryChanged(""))
        advanceUntilIdle()

        // THEN
        assertEquals(2, vm.viewState.value.holdings.size)
    }

    @Test
    fun `SHOULD return empty list WHEN no matches GIVEN search for nonexistent ticker`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(apple, microsoft))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(PortfolioContract.PortfolioViewEvent.SearchQueryChanged("NONEXISTENT"))
        advanceUntilIdle()

        // THEN
        assertTrue(vm.viewState.value.holdings.isEmpty())
    }

    // ─── Sorting ──────────────────────────────────────────────────────────

    @Test
    fun `SHOULD sort highest gain first WHEN sort by gain desc GIVEN holdings loaded`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(microsoft, apple))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN — default sort is GAIN DESC
        val state = vm.viewState.value

        // THEN
        assertEquals("AAPL", state.holdings[0].holding.tickerId)
        assertEquals("MSFT", state.holdings[1].holding.tickerId)
    }

    @Test
    fun `SHOULD sort lowest gain first WHEN sort by gain asc GIVEN holdings loaded`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(apple, microsoft))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(PortfolioContract.PortfolioViewEvent.SortOrderChanged(SortOrder(SortField.GAIN, ascending = true)))
        advanceUntilIdle()

        // THEN
        assertEquals("MSFT", vm.viewState.value.holdings[0].holding.tickerId)
        assertEquals("AAPL", vm.viewState.value.holdings[1].holding.tickerId)
    }

    @Test
    fun `SHOULD sort highest yield first WHEN sort by yield desc GIVEN holdings loaded`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(apple, microsoft))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(PortfolioContract.PortfolioViewEvent.SortOrderChanged(SortOrder(SortField.YIELD, ascending = false)))
        advanceUntilIdle()

        // THEN
        assertEquals("MSFT", vm.viewState.value.holdings[0].holding.tickerId)
        assertEquals("AAPL", vm.viewState.value.holdings[1].holding.tickerId)
    }

    @Test
    fun `SHOULD sort lowest yield first WHEN sort by yield asc GIVEN holdings loaded`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(apple, microsoft))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(PortfolioContract.PortfolioViewEvent.SortOrderChanged(SortOrder(SortField.YIELD, ascending = true)))
        advanceUntilIdle()

        // THEN
        assertEquals("AAPL", vm.viewState.value.holdings[0].holding.tickerId)
        assertEquals("MSFT", vm.viewState.value.holdings[1].holding.tickerId)
    }

    @Test
    fun `SHOULD sort newest first WHEN sort by date desc GIVEN holdings loaded`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(apple, microsoft))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(PortfolioContract.PortfolioViewEvent.SortOrderChanged(SortOrder(SortField.DATE, ascending = false)))
        advanceUntilIdle()

        // THEN
        assertEquals("MSFT", vm.viewState.value.holdings[0].holding.tickerId)
        assertEquals("AAPL", vm.viewState.value.holdings[1].holding.tickerId)
    }

    @Test
    fun `SHOULD sort oldest first WHEN sort by date asc GIVEN holdings loaded`() = runTest {
        // GIVEN
        every { mockUseCase() } returns flowOf(listOf(microsoft, apple))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        vm.onViewEvent(PortfolioContract.PortfolioViewEvent.SortOrderChanged(SortOrder(SortField.DATE, ascending = true)))
        advanceUntilIdle()

        // THEN
        assertEquals("AAPL", vm.viewState.value.holdings[0].holding.tickerId)
        assertEquals("MSFT", vm.viewState.value.holdings[1].holding.tickerId)
    }

    // ─── SortOrder toggle ─────────────────────────────────────────────────

    @Test
    fun `SHOULD toggle direction WHEN same field clicked GIVEN current sort order`() {
        // GIVEN
        val gainDesc = SortOrder(SortField.GAIN, ascending = false)

        // WHEN
        val toggled = gainDesc.toggle(SortField.GAIN)

        // THEN
        assertEquals(SortOrder(SortField.GAIN, ascending = true), toggled)
    }

    @Test
    fun `SHOULD reset to desc WHEN different field clicked GIVEN current sort order`() {
        // GIVEN
        val gainAsc = SortOrder(SortField.GAIN, ascending = true)

        // WHEN
        val switched = gainAsc.toggle(SortField.YIELD)

        // THEN
        assertEquals(SortOrder(SortField.YIELD, ascending = false), switched)
    }
}
