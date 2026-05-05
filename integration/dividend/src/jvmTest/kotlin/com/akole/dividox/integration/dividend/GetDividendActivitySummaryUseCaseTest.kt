package com.akole.dividox.integration.dividend

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import com.akole.dividox.integration.dividend.domain.usecase.GetDividendActivitySummaryUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlinx.datetime.todayIn

class GetDividendActivitySummaryUseCaseTest {

    private val dividendRepo: DividendRepository = mockk()
    private val portfolioRepo: PortfolioRepository = mockk()
    private val marketRepo: MarketRepository = mockk()

    private val useCase = GetDividendActivitySummaryUseCase(
        dividendRepository = dividendRepo,
        portfolioRepository = portfolioRepo,
        marketRepository = marketRepo,
    )

    private fun stubDefaults(
        lifetime: Double = 0.0,
        ytd: Double = 0.0,
        history: List<DividendPayment> = emptyList(),
        upcoming: List<DividendPayment> = emptyList(),
        holdings: List<Holding> = emptyList(),
    ) {
        every { dividendRepo.getLifetimeDividends() } returns flowOf(lifetime)
        every { dividendRepo.getYtdDividends() } returns flowOf(ytd)
        every { dividendRepo.getDividendHistory() } returns flowOf(history)
        every { dividendRepo.getUpcomingPayments() } returns flowOf(upcoming)
        every { portfolioRepo.observePortfolio() } returns flowOf(Result.success(holdings))
    }

    // ── GIVEN lifetime and ytd WHEN invoked THEN summary reflects those values ─
    @Test
    fun `GIVEN lifetime and ytd WHEN invoked THEN summary reflects those values`() = runTest {
        stubDefaults(lifetime = 1000.0, ytd = 200.0)

        val result = useCase().first()

        assertEquals(1000.0, result.lifetime)
        assertEquals(200.0, result.ytd)
    }

    // ── GIVEN no upcoming payments WHEN invoked THEN nextPayout is null ───────
    @Test
    fun `GIVEN no upcoming payments WHEN invoked THEN nextPayout is null`() = runTest {
        stubDefaults()

        assertNull(useCase().first().nextPayout)
    }

    // ── GIVEN upcoming payment with company info WHEN invoked THEN enriched ───
    @Test
    fun `GIVEN upcoming payment and company info WHEN invoked THEN nextPayout is enriched`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val futureDate = LocalDate(today.year + 1, today.month, 1)
        val payment = payment("p1", "AAPL", 50.0, futureDate)
        stubDefaults(upcoming = listOf(payment))
        coEvery { marketRepo.getCompanyInfo("AAPL") } returns
            Result.success(companyInfo("AAPL", "Apple Inc."))

        val result = useCase().first()

        assertNotNull(result.nextPayout)
        assertEquals("AAPL", result.nextPayout!!.payment.tickerId)
        assertEquals("Apple Inc.", result.nextPayout!!.companyInfo?.name)
    }

    // ── GIVEN market fails WHEN invoked THEN nextPayout has null companyInfo ───
    @Test
    fun `GIVEN market error WHEN invoked THEN nextPayout companyInfo is null`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val payment = payment("p1", "AAPL", 50.0, LocalDate(today.year + 1, today.month, 1))
        stubDefaults(upcoming = listOf(payment))
        coEvery { marketRepo.getCompanyInfo("AAPL") } returns
            Result.failure(RuntimeException("not found"))

        assertNull(useCase().first().nextPayout!!.companyInfo)
    }

    // ── GIVEN empty portfolio WHEN invoked THEN yoc is zero ───────────────────
    @Test
    fun `GIVEN empty portfolio WHEN invoked THEN yoc is zero`() = runTest {
        stubDefaults(ytd = 100.0)

        assertEquals(0.0, useCase().first().yoc)
    }

    // ── GIVEN holdings and ytd WHEN invoked THEN yoc is positive ─────────────
    @Test
    fun `GIVEN holdings and ytd dividends WHEN invoked THEN yoc is positive`() = runTest {
        stubDefaults(
            ytd = 500.0,
            holdings = listOf(holding("h1", "AAPL", shares = 10.0, purchasePrice = 100.0)),
        )

        assertTrue(useCase().first().yoc > 0.0)
    }

    // ── GIVEN no prior year history WHEN invoked THEN yoyPercent is null ──────
    @Test
    fun `GIVEN no prior year payments WHEN invoked THEN yoyPercent is null`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        stubDefaults(
            history = listOf(payment("p1", "AAPL", 100.0, LocalDate(today.year, today.month, 1)))
        )

        assertNull(useCase().first().yoyPercent)
    }

    // ── GIVEN both years have data WHEN invoked THEN yoyPercent is computed ───
    @Test
    fun `GIVEN payments in both years WHEN invoked THEN yoyPercent is computed`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        stubDefaults(
            history = listOf(
                payment("p1", "AAPL", 100.0, LocalDate(today.year, today.month, 1)),
                payment("p2", "AAPL", 80.0, LocalDate(today.year - 1, today.month, 1)),
            )
        )

        val yoy = useCase().first().yoyPercent
        assertNotNull(yoy)
        assertEquals(25.0, yoy, 0.01)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

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

    private fun holding(id: String, ticker: String, shares: Double, purchasePrice: Double) =
        Holding(
            id = HoldingId(id),
            tickerId = ticker,
            shares = shares,
            purchasePrice = purchasePrice,
            purchaseCurrency = Currency.USD,
            purchaseDate = 0L,
        )

    private fun companyInfo(ticker: String, name: String) =
        CompanyInfo(ticker = ticker, name = name, exchange = "NASDAQ", logoUrl = null)
}
