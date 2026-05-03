package com.akole.dividox.integration.dividend

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedUpcomingPaymentsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetEnrichedUpcomingPaymentsUseCaseTest {

    private val dividendRepo: DividendRepository = mockk()
    private val marketRepo: MarketRepository = mockk()

    private val useCase = GetEnrichedUpcomingPaymentsUseCase(
        dividendRepository = dividendRepo,
        marketRepository = marketRepo,
    )

    // ── GIVEN no upcoming payments WHEN invoked THEN returns empty list ───────
    @Test
    fun `GIVEN empty upcoming payments WHEN invoked THEN returns empty list`() = runTest {
        every { dividendRepo.getUpcomingPayments() } returns flowOf(emptyList())

        assertEquals(emptyList(), useCase().first())
    }

    // ── GIVEN payments with company info WHEN invoked THEN enriched ───────────
    @Test
    fun `GIVEN upcoming payments with company info WHEN invoked THEN enriched list returned`() = runTest {
        val p1 = payment("p1", "AAPL", LocalDate(2025, 6, 1))
        val p2 = payment("p2", "MSFT", LocalDate(2025, 7, 1))
        every { dividendRepo.getUpcomingPayments() } returns flowOf(listOf(p1, p2))
        coEvery { marketRepo.getCompanyInfo("AAPL") } returns
            Result.success(companyInfo("AAPL", "Apple Inc."))
        coEvery { marketRepo.getCompanyInfo("MSFT") } returns
            Result.success(companyInfo("MSFT", "Microsoft"))

        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals("Apple Inc.", result[0].companyInfo?.name)
        assertEquals("Microsoft", result[1].companyInfo?.name)
    }

    // ── GIVEN market fails for ticker WHEN invoked THEN companyInfo null ──────
    @Test
    fun `GIVEN market error for ticker WHEN invoked THEN companyInfo is null`() = runTest {
        every { dividendRepo.getUpcomingPayments() } returns
            flowOf(listOf(payment("p1", "UNKNOWN", LocalDate(2025, 8, 1))))
        coEvery { marketRepo.getCompanyInfo("UNKNOWN") } returns
            Result.failure(RuntimeException("not found"))

        val result = useCase().first()

        assertEquals(1, result.size)
        assertNull(result[0].companyInfo)
    }

    // ── GIVEN unsorted payments WHEN invoked THEN sorted ascending by date ────
    @Test
    fun `GIVEN unsorted upcoming payments WHEN invoked THEN sorted ascending by date`() = runTest {
        val later = payment("p1", "AAPL", LocalDate(2025, 9, 1))
        val sooner = payment("p2", "MSFT", LocalDate(2025, 6, 1))
        every { dividendRepo.getUpcomingPayments() } returns flowOf(listOf(later, sooner))
        coEvery { marketRepo.getCompanyInfo(any()) } returns
            Result.failure(RuntimeException())

        val result = useCase().first()

        assertEquals(LocalDate(2025, 6, 1), result[0].payment.paymentDate)
        assertEquals(LocalDate(2025, 9, 1), result[1].payment.paymentDate)
    }

    // ── GIVEN duplicate ticker WHEN invoked THEN company info reused ──────────
    @Test
    fun `GIVEN duplicate ticker WHEN invoked THEN company info is reused`() = runTest {
        val p1 = payment("p1", "AAPL", LocalDate(2025, 6, 1))
        val p2 = payment("p2", "AAPL", LocalDate(2025, 9, 1))
        every { dividendRepo.getUpcomingPayments() } returns flowOf(listOf(p1, p2))
        coEvery { marketRepo.getCompanyInfo("AAPL") } returns
            Result.success(companyInfo("AAPL", "Apple Inc."))

        val result = useCase().first()

        assertEquals(2, result.size)
        result.forEach { assertEquals("Apple Inc.", it.companyInfo?.name) }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun payment(id: String, ticker: String, date: LocalDate) =
        DividendPayment(
            id = DividendPaymentId(id),
            tickerId = ticker,
            amount = 25.0,
            currency = "USD",
            paymentDate = date,
        )

    private fun companyInfo(ticker: String, name: String) =
        CompanyInfo(ticker = ticker, name = name, exchange = "NASDAQ", logoUrl = null)
}
