package com.akole.dividox.feature.dividends

import com.akole.dividox.common.network.connectivity.NetworkConnectivityManager
import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.feature.dividends.DividendsContract.DividendsSideEffect
import com.akole.dividox.feature.dividends.DividendsContract.DividendsViewEvent
import com.akole.dividox.integration.dividend.domain.model.DividendActivitySummary
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import com.akole.dividox.integration.dividend.domain.model.MonthBar
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendActivitySummaryUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendProjectionBarsUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedPaymentHistoryUseCase
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedUpcomingPaymentsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

@OptIn(ExperimentalCoroutinesApi::class)
class DividendsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getDividendActivitySummary: GetDividendActivitySummaryUseCase = mockk()
    private val getDividendProjectionBars: GetDividendProjectionBarsUseCase = mockk()
    private val getEnrichedUpcomingPayments: GetEnrichedUpcomingPaymentsUseCase = mockk()
    private val getEnrichedPaymentHistory: GetEnrichedPaymentHistoryUseCase = mockk()
    private val connectivityManager: NetworkConnectivityManager = mockk()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { getDividendActivitySummary() } returns emptyFlow()
        every { getDividendProjectionBars() } returns emptyFlow()
        every { getEnrichedUpcomingPayments() } returns emptyFlow()
        every { getEnrichedPaymentHistory() } returns emptyFlow()
        every { connectivityManager.observeConnectivity() } returns emptyFlow()
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = DividendsViewModel(
        getDividendActivitySummary = getDividendActivitySummary,
        getDividendProjectionBars = getDividendProjectionBars,
        getEnrichedUpcomingPayments = getEnrichedUpcomingPayments,
        getEnrichedPaymentHistory = getEnrichedPaymentHistory,
        connectivityManager = connectivityManager,
    )

    // ─── GIVEN/WHEN/THEN ────────────────────────────────────────────────────

    @Test
    fun `GIVEN empty flows WHEN init THEN state isLoading becomes false with empty data`() = runTest {
        every { getDividendActivitySummary() } returns flowOf(DividendActivitySummary.Empty)
        every { getDividendProjectionBars() } returns flowOf(emptyList())
        every { getEnrichedUpcomingPayments() } returns flowOf(emptyList())
        every { getEnrichedPaymentHistory() } returns flowOf(emptyList())

        val vm = viewModel()
        advanceUntilIdle()

        assertFalse(vm.viewState.value.isLoading)
        assertEquals(DividendActivitySummary.Empty, vm.viewState.value.summary)
        assertTrue(vm.viewState.value.projectionBars.isEmpty())
        assertTrue(vm.viewState.value.upcomingPayments.isEmpty())
        assertTrue(vm.viewState.value.historyByMonth.isEmpty())
        assertNull(vm.viewState.value.error)
    }

    @Test
    fun `GIVEN payments in two months WHEN init THEN historyByMonth grouped correctly`() = runTest {
        val jan = LocalDate(2024, Month.JANUARY, 15)
        val feb = LocalDate(2024, Month.FEBRUARY, 10)
        val payments = listOf(
            enrichedPayment("AAPL", jan),
            enrichedPayment("MSFT", jan),
            enrichedPayment("GOOG", feb),
        )
        every { getDividendActivitySummary() } returns flowOf(DividendActivitySummary.Empty)
        every { getDividendProjectionBars() } returns flowOf(emptyList())
        every { getEnrichedUpcomingPayments() } returns flowOf(emptyList())
        every { getEnrichedPaymentHistory() } returns flowOf(payments)

        val vm = viewModel()
        advanceUntilIdle()

        val history = vm.viewState.value.historyByMonth
        assertEquals(2, history.size)
        assertEquals(2, history[LocalDate(2024, Month.JANUARY, 1)]?.size)
        assertEquals(1, history[LocalDate(2024, Month.FEBRUARY, 1)]?.size)
    }

    @Test
    fun `GIVEN payments WHEN init THEN most recent month is in expandedMonths`() = runTest {
        val jan = LocalDate(2024, Month.JANUARY, 15)
        val feb = LocalDate(2024, Month.FEBRUARY, 10)
        val payments = listOf(enrichedPayment("AAPL", jan), enrichedPayment("GOOG", feb))
        every { getDividendActivitySummary() } returns flowOf(DividendActivitySummary.Empty)
        every { getDividendProjectionBars() } returns flowOf(emptyList())
        every { getEnrichedUpcomingPayments() } returns flowOf(emptyList())
        every { getEnrichedPaymentHistory() } returns flowOf(payments)

        val vm = viewModel()
        advanceUntilIdle()

        val mostRecent = LocalDate(2024, Month.FEBRUARY, 1)
        assertTrue(mostRecent in vm.viewState.value.expandedMonths)
    }

    @Test
    fun `GIVEN expanded month WHEN MonthToggled THEN month is collapsed`() = runTest {
        val jan = LocalDate(2024, Month.JANUARY, 15)
        every { getDividendActivitySummary() } returns flowOf(DividendActivitySummary.Empty)
        every { getDividendProjectionBars() } returns flowOf(emptyList())
        every { getEnrichedUpcomingPayments() } returns flowOf(emptyList())
        every { getEnrichedPaymentHistory() } returns flowOf(listOf(enrichedPayment("AAPL", jan)))

        val vm = viewModel()
        advanceUntilIdle()

        val janKey = LocalDate(2024, Month.JANUARY, 1)
        assertTrue(janKey in vm.viewState.value.expandedMonths)

        vm.onViewEvent(DividendsViewEvent.MonthToggled(janKey))

        assertFalse(janKey in vm.viewState.value.expandedMonths)
    }

    @Test
    fun `GIVEN collapsed month WHEN MonthToggled THEN month is expanded`() = runTest {
        val jan = LocalDate(2024, Month.JANUARY, 15)
        val feb = LocalDate(2024, Month.FEBRUARY, 10)
        every { getDividendActivitySummary() } returns flowOf(DividendActivitySummary.Empty)
        every { getDividendProjectionBars() } returns flowOf(emptyList())
        every { getEnrichedUpcomingPayments() } returns flowOf(emptyList())
        every { getEnrichedPaymentHistory() } returns flowOf(listOf(enrichedPayment("AAPL", jan), enrichedPayment("GOOG", feb)))

        val vm = viewModel()
        advanceUntilIdle()

        val janKey = LocalDate(2024, Month.JANUARY, 1)
        // Only feb is expanded initially (most recent)
        assertFalse(janKey in vm.viewState.value.expandedMonths)

        vm.onViewEvent(DividendsViewEvent.MonthToggled(janKey))

        assertTrue(janKey in vm.viewState.value.expandedMonths)
    }

    @Test
    fun `GIVEN ticker WHEN PaymentClicked THEN NavigateToSecurity side effect emitted`() = runTest {
        every { getDividendActivitySummary() } returns flowOf(DividendActivitySummary.Empty)
        every { getDividendProjectionBars() } returns flowOf(emptyList())
        every { getEnrichedUpcomingPayments() } returns flowOf(emptyList())
        every { getEnrichedPaymentHistory() } returns flowOf(emptyList())

        val vm = viewModel()
        advanceUntilIdle()

        var capturedEffect: DividendsSideEffect? = null
        val job = this.launch {
            vm.sideEffect.collect { capturedEffect = it }
        }

        vm.onViewEvent(DividendsViewEvent.PaymentClicked("AAPL"))
        advanceUntilIdle()

        val nav = capturedEffect as? DividendsSideEffect.Navigation.NavigateToSecurity
        assertEquals("AAPL", nav?.ticker)
        job.cancel()
    }

    @Test
    fun `GIVEN offline WHEN connectivity becomes false THEN state isOffline true`() = runTest {
        every { connectivityManager.observeConnectivity() } returns flowOf(false)

        val vm = viewModel()
        advanceUntilIdle()

        assertTrue(vm.viewState.value.isOffline)
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private fun enrichedPayment(ticker: String, date: LocalDate): EnrichedPayment =
        EnrichedPayment(
            payment = DividendPayment(
                id = DividendPaymentId("$ticker-${date}"),
                tickerId = ticker,
                amount = 1.0,
                amountPerShare = 0.0,
                shares = 0.0,
                currency = "USD",
                paymentDate = date,
            ),
            companyInfo = null,
        )
}
