package com.akole.dividox.integration.dividend

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendProjectionBarsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.todayIn
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock

class GetDividendProjectionBarsUseCaseTest {

    private val dividendRepo: DividendRepository = mockk()

    private fun useCase(past: Int = 6, future: Int = 2) = GetDividendProjectionBarsUseCase(
        dividendRepository = dividendRepo,
        pastMonths = past,
        futureMonths = future,
    )

    private fun stubEmpty() {
        every { dividendRepo.getDividendHistory() } returns flowOf(emptyList())
        every { dividendRepo.getUpcomingPayments() } returns flowOf(emptyList())
    }

    // GIVEN no data WHEN invoked THEN all bars are zero
    @Test
    fun `GIVEN no data WHEN invoked THEN returns expected bar count with zero amounts`() = runTest {
        stubEmpty()
        val past = 6; val future = 2
        val result = useCase(past, future)().first()

        assertEquals(past + future, result.size)
        assertTrue(result.all { it.amount == 0.0 })
    }

    // GIVEN past payment WHEN invoked THEN bar is not projected
    @Test
    fun `GIVEN past payment WHEN invoked THEN corresponding bar is not projected`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val thisMonthKey = LocalDate(today.year, today.month, 1)
        every { dividendRepo.getDividendHistory() } returns
            flowOf(listOf(payment("p1", "AAPL", 75.0, thisMonthKey)))
        every { dividendRepo.getUpcomingPayments() } returns flowOf(emptyList())

        val result = useCase(past = 3, future = 1)().first()
        val bar = result.first { it.yearMonth == thisMonthKey }

        assertEquals(75.0, bar.amount)
        assertFalse(bar.isProjected)
    }

    // GIVEN future payment WHEN invoked THEN bar is projected
    @Test
    fun `GIVEN future payment WHEN invoked THEN corresponding bar is projected`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val nextYear = if (today.monthNumber == 12) today.year + 1 else today.year
        val nextMonth = if (today.monthNumber == 12) 1 else today.monthNumber + 1
        val futureDate = LocalDate(nextYear, nextMonth, 15)
        val futureKey = LocalDate(nextYear, nextMonth, 1)

        every { dividendRepo.getDividendHistory() } returns flowOf(emptyList())
        every { dividendRepo.getUpcomingPayments() } returns
            flowOf(listOf(payment("p1", "AAPL", 50.0, futureDate)))

        val result = useCase(past = 2, future = 2)().first()
        val bar = result.first { it.yearMonth == futureKey }

        assertEquals(50.0, bar.amount)
        assertTrue(bar.isProjected)
    }

    // GIVEN multiple payments same month WHEN invoked THEN amounts summed
    @Test
    fun `GIVEN multiple payments in same month WHEN invoked THEN amounts are summed`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val thisMonthKey = LocalDate(today.year, today.month, 1)
        every { dividendRepo.getDividendHistory() } returns flowOf(listOf(
            payment("p1", "AAPL", 40.0, LocalDate(today.year, today.month, 5)),
            payment("p2", "MSFT", 60.0, LocalDate(today.year, today.month, 20)),
        ))
        every { dividendRepo.getUpcomingPayments() } returns flowOf(emptyList())

        val result = useCase(past = 2, future = 1)().first()
        val bar = result.first { it.yearMonth == thisMonthKey }

        assertEquals(100.0, bar.amount)
    }

    // WHEN invoked THEN bars are sorted chronologically
    @Test
    fun `WHEN invoked THEN bars are sorted chronologically oldest first`() = runTest {
        stubEmpty()
        val dates = useCase(past = 4, future = 2)().first().map { it.yearMonth }
        assertEquals(dates.sorted(), dates)
    }

    // Helpers
    private fun payment(id: String, ticker: String, amount: Double, date: LocalDate) =
        DividendPayment(
            id = DividendPaymentId(id),
            tickerId = ticker,
            amount = amount,
            amountPerShare = 0.0,
            shares = 0.0,
            currency = "USD",
            paymentDate = date,
        )
}
