package com.akole.dividox.integration.security.usecase

import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.integration.security.FakeMarketRepository
import com.akole.dividox.integration.security.FakePortfolioRepository
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioSummaryUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetPortfolioSummaryUseCaseTest {

    private val portfolioRepo = FakePortfolioRepository()
    private val marketRepo = FakeMarketRepository()

    private val getPortfolioWithQuotesUseCase = GetPortfolioWithQuotesUseCase(
        getPortfolioUseCase = GetPortfolioUseCase(portfolioRepo),
        getMultipleQuotesUseCase = GetMultipleQuotesUseCase(marketRepo),
        getDividendInfoUseCase = GetDividendInfoUseCase(marketRepo),
    )

    private val sut = GetPortfolioSummaryUseCase(getPortfolioWithQuotesUseCase)

    @Test
    fun `SHOULD emit zero summary WHEN portfolio is empty GIVEN no holdings`() = runTest {
        // GIVEN — empty portfolio

        // WHEN
        val summary = sut().first()

        // THEN
        assertEquals(0.0, summary.totalValue)
        assertEquals(0.0, summary.totalGain)
        assertEquals(0.0, summary.totalGainPercent)
        assertEquals(0.0, summary.totalYield)
        assertEquals(0.0, summary.dividendsCollected)
    }

    @Test
    fun `SHOULD compute totalValue as sum of currentValue WHEN holdings exist GIVEN valid quotes`() = runTest {
        // GIVEN — 10 AAPL @ $150 = $1500, 5 MSFT @ $300 = $1500 → total $3000
        portfolioRepo.setHoldings(
            listOf(
                FakePortfolioRepository.holding(id = "h1", tickerId = "AAPL", shares = 10.0, purchasePrice = 100.0),
                FakePortfolioRepository.holding(id = "h2", tickerId = "MSFT", shares = 5.0, purchasePrice = 200.0),
            ),
        )
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 150.0))
        marketRepo.setQuote("MSFT", FakeMarketRepository.quote(ticker = "MSFT", price = 300.0))
        marketRepo.setDividendInfo("AAPL", FakeMarketRepository.dividendInfo("AAPL"))
        marketRepo.setDividendInfo("MSFT", FakeMarketRepository.dividendInfo("MSFT"))

        // WHEN
        val summary = sut().first()

        // THEN
        assertEquals(3000.0, summary.totalValue, 0.0001)
    }

    @Test
    fun `SHOULD compute totalGain correctly WHEN price increased GIVEN purchase and current price`() = runTest {
        // GIVEN — bought 10 AAPL at $100 = $1000 cost, now $150 = $1500 current → gain $500
        portfolioRepo.setHoldings(
            listOf(
                FakePortfolioRepository.holding(tickerId = "AAPL", shares = 10.0, purchasePrice = 100.0),
            ),
        )
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 150.0))
        marketRepo.setDividendError("AAPL", RuntimeException("no dividends"))

        // WHEN
        val summary = sut().first()

        // THEN
        assertEquals(500.0, summary.totalGain, 0.0001)
        assertEquals(50.0, summary.totalGainPercent, 0.0001)
    }

    @Test
    fun `SHOULD compute dividendsCollected from annualPayout WHEN dividendInfo available GIVEN holdings with dividends`() = runTest {
        // GIVEN — 10 AAPL, annualPayout = $0.96 → dividendsCollected = 10 * 0.96 = $9.60
        portfolioRepo.setHoldings(
            listOf(
                FakePortfolioRepository.holding(tickerId = "AAPL", shares = 10.0, purchasePrice = 100.0),
            ),
        )
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 150.0))
        marketRepo.setDividendInfo("AAPL", FakeMarketRepository.dividendInfo(ticker = "AAPL", annualPayout = 0.96))

        // WHEN
        val summary = sut().first()

        // THEN
        assertEquals(9.6, summary.dividendsCollected, 0.0001)
    }
}
