package com.akole.dividox.integration.security.usecase

import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.integration.security.FakeMarketRepository
import com.akole.dividox.integration.security.FakePortfolioRepository
import com.akole.dividox.integration.security.domain.usecase.GetSecurityHoldingUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GetSecurityHoldingUseCaseTest {

    private val portfolioRepo = FakePortfolioRepository()
    private val marketRepo = FakeMarketRepository()

    private val sut = GetSecurityHoldingUseCase(
        getPortfolioUseCase = GetPortfolioUseCase(portfolioRepo),
        getStockQuoteUseCase = GetStockQuoteUseCase(marketRepo),
        getDividendInfoUseCase = GetDividendInfoUseCase(marketRepo),
    )

    @Test
    fun `SHOULD return null WHEN ticker not in portfolio GIVEN empty portfolio`() = runTest {
        // GIVEN — empty portfolio

        // WHEN
        val result = sut("AAPL").first()

        // THEN
        assertNull(result)
    }

    @Test
    fun `SHOULD return SecurityHolding WHEN ticker is in portfolio GIVEN valid quote`() = runTest {
        // GIVEN
        portfolioRepo.setHoldings(listOf(FakePortfolioRepository.holding(tickerId = "AAPL")))
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL"))
        marketRepo.setDividendInfo("AAPL", FakeMarketRepository.dividendInfo())

        // WHEN
        val result = sut("AAPL").first()

        // THEN
        assertNotNull(result)
        assertEquals("AAPL", result.holding.tickerId)
        assertEquals("AAPL", result.quote.ticker)
    }

    @Test
    fun `SHOULD return null WHEN ticker differs from held tickers GIVEN other ticker in portfolio`() = runTest {
        // GIVEN
        portfolioRepo.setHoldings(listOf(FakePortfolioRepository.holding(tickerId = "MSFT")))
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote())

        // WHEN
        val result = sut("AAPL").first()

        // THEN
        assertNull(result)
    }

    @Test
    fun `SHOULD set dividendInfo to null WHEN dividend fetch fails GIVEN market error`() = runTest {
        // GIVEN
        portfolioRepo.setHoldings(listOf(FakePortfolioRepository.holding(tickerId = "AAPL")))
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote())
        marketRepo.setDividendError("AAPL", RuntimeException("no dividends"))

        // WHEN
        val result = sut("AAPL").first()

        // THEN
        assertNotNull(result)
        assertNull(result.dividendInfo)
    }

    @Test
    fun `SHOULD compute totalGainPercent WHEN quote price differs from purchase price GIVEN valid data`() = runTest {
        // GIVEN — bought at $100, now $200 → +100%
        portfolioRepo.setHoldings(
            listOf(FakePortfolioRepository.holding(tickerId = "AAPL", purchasePrice = 100.0)),
        )
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 200.0))
        marketRepo.setDividendError("AAPL", RuntimeException())

        // WHEN
        val result = sut("AAPL").first()

        // THEN
        assertNotNull(result)
        assertEquals(100.0, result.totalGainPercent, 0.0001)
    }
}
