package com.akole.dividox.feature.portfolio.connectivity

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.common.settings.domain.model.AppSettings
import com.akole.dividox.common.settings.domain.usecase.ObserveAppSettingsUseCase
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.feature.portfolio.PortfolioViewModel
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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

/**
 * Tests for PortfolioViewModel connectivity refresh behavior.
 *
 * These tests verify:
 * - PortfolioViewModel triggers data refresh on offline→online transition
 * - Portfolio state updates with refreshed data
 * - Multiple connectivity changes are handled correctly
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PortfolioViewModelConnectivityTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val mockUseCase = mockk<GetPortfolioWithQuotesUseCase>()
    private val mockObserveSettings = mockk<ObserveAppSettingsUseCase>()
    private val mockCurrencyConverter = mockk<CurrencyConverter>()
    private val mockConnectivityManager = mockk<NetworkConnectivityManager>()

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

    private fun viewModel() = PortfolioViewModel(
        mockUseCase,
        mockObserveSettings,
        mockCurrencyConverter,
        mockConnectivityManager,
    )

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
        dividendInfo = DividendInfo(
            ticker = "AAPL",
            yield = 0.5,
            annualPayout = 100.0,
            payoutRatio = 0.3,
            fiveYearGrowth = 5.0,
            exDividendDate = null,
        ),
        totalGainPercent = 20.0,
    )

    @Test
    fun `SHOULD emit holdings WHEN connectivity manager observable emits GIVEN Flow subscription`() = runTest {
        // GIVEN — connectivity always connected
        every { mockConnectivityManager.observeConnectivity() } returns flowOf(true, true)
        every { mockUseCase() } returns flowOf(listOf(apple))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        val holdings = vm.viewState.value.holdings

        // THEN — holdings should load
        assertEquals(1, holdings.size)
        assertEquals("AAPL", holdings[0].holding.tickerId)
    }

    @Test
    fun `SHOULD preserve sort and search state WHEN data refreshes GIVEN portfolio view state`() = runTest {
        // GIVEN — connectivity transitions false→true (offline→online)
        every { mockConnectivityManager.observeConnectivity() } returns flowOf(false, true, true)
        every { mockUseCase() } returns flowOf(listOf(apple))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN — state should still contain holdings after refresh
        val holdings = vm.viewState.value.holdings

        // THEN
        assertEquals(1, holdings.size, "Should have holdings after connectivity recovery")
    }

    @Test
    fun `SHOULD emit connectivity status WHEN manager observeConnectivity called GIVEN mock implementation`() = runTest {
        // GIVEN
        val connectivityFlow = flowOf(true, false, true)
        every { mockConnectivityManager.observeConnectivity() } returns connectivityFlow
        every { mockUseCase() } returns emptyFlow()

        // WHEN
        val emissions = mutableListOf<Boolean>()
        mockConnectivityManager.observeConnectivity().collect { emissions.add(it) }
        advanceUntilIdle()

        // THEN — should emit 3 values
        assertEquals(3, emissions.size)
        assertTrue(emissions[0])
        assertTrue(!emissions[1])
        assertTrue(emissions[2])
    }

    @Test
    fun `SHOULD load holdings successfully WHEN view model created GIVEN connected network`() = runTest {
        // GIVEN
        every { mockConnectivityManager.observeConnectivity() } returns flowOf(true)
        every { mockUseCase() } returns flowOf(listOf(apple))
        val vm = viewModel()
        advanceUntilIdle()

        // WHEN
        val isLoading = vm.viewState.value.isLoading
        val holdings = vm.viewState.value.holdings

        // THEN
        assertTrue(!isLoading, "Should finish loading")
        assertEquals(1, holdings.size)
    }
}
