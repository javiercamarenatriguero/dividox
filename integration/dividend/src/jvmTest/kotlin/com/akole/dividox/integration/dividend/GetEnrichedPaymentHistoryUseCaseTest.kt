package com.akole.dividox.integration.dividend

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.integration.dividend.domain.usecase.GetEnrichedPaymentHistoryUseCase
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

class GetEnrichedPaymentHistoryUseCaseTest {

    private val dividendRepo: DividendRepository = mockk()
    private val marketRepo: MarketRepository = mockk()

    private val useCase = GetEnrichedPaymentHistoryUseCase(
        dividendRepository = dividendRepo,
        marketRepository = marketRepo,
    )

    // ── GIVEN empty history WHEN invoked THEN returns empty list ─────────────
    @Test
    fun `GIVEN empty history WHEN invoked THEN returns empty list`() = runTest {
        every { dividendRepo.getDividendHistory() } returns flowOf(emptyList())

        assertEquals(emptyList(), useCase().first())
    }

    // ── GIVEN payments with company info WHEN invoked THEN enriched ───────────
    @Test
    fun `GIVEN payments with company info WHEN invoked THEN enriched and most recent first`() = runTest {
        every { dividendRepo.getDividendHistory() } returns flowOf(listOf(
            payment("p1", "AAPL", LocalDate(2024, 3, 15)),
            payment("p2", "MSFT", LocalDate(2024, 6, 20)),
        ))
        coEvery { marketRepo.getCompanyInfo("AAPL") } returns
            Result.success(companyInfo("AAPL", "Apple Inc."))
        coEvery { marketRepo.getCompanyInfo("MSFT") } returns
            Result.success(companyInfo("MSFT", "Microsoft"))

        val result = useCase().first()

        assertEquals(2, result.size)
        assertEquals("Microsoft", result[0].companyInfo?.name)  // most recent first
        assertEquals("Apple Inc.", result[1].companyInfo?.name)
    }

    // ── GIVEN unsorted history WHEN invoked THEN sorted descending ───────────
    @Test
    fun `GIVEN unsorted history WHEN invoked THEN sorted descending by date`() = runTest {
        val older = payment("p1", "AAPL", LocalDate(2024, 1, 10))
        val newer = payment("p2", "AAPL", LocalDate(2024, 11, 5))
        every { dividendRepo.getDividendHistory() } returns flowOf(listOf(older, newer))
        coEvery { marketRepo.getCompanyInfo("AAPL") } returns Result.failure(RuntimeException())

        val result = useCase().first()

        assertEquals(LocalDate(2024, 11, 5), result[0].payment.paymentDate)
        assertEquals(LocalDate(2024, 1, 10), result[1].payment.paymentDate)
    }

    // ── GIVEN market error WHEN invoked THEN companyInfo is null ─────────────
    @Test
    fun `GIVEN market error WHEN invoked THEN companyInfo is null for that ticker`() = runTest {
        every { dividendRepo.getDividendHistory() } returns
            flowOf(listOf(payment("p1", "UNKNOWN", LocalDate(2024, 5, 1))))
        coEvery { marketRepo.getCompanyInfo("UNKNOWN") } returns
            Result.failure(RuntimeException("not found"))

        val result = useCase().first()

        assertEquals(1, result.size)
        assertNull(result[0].companyInfo)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun payment(id: String, ticker: String, date: LocalDate) =
        DividendPayment(
            id = DividendPaymentId(id),
            tickerId = ticker,
            amount = 30.0,
            amountPerShare = 0.0,
            shares = 0.0,
            currency = "USD",
            paymentDate = date,
        )

    private fun companyInfo(ticker: String, name: String) =
        CompanyInfo(ticker = ticker, name = name, exchange = "NASDAQ", logoUrl = null)
}
