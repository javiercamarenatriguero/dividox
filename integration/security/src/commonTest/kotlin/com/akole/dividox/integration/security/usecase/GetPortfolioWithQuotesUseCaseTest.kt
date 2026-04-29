package com.akole.dividox.integration.security.usecase

import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.integration.security.FakeMarketRepository
import com.akole.dividox.integration.security.FakePortfolioRepository
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetPortfolioWithQuotesUseCaseTest {

    private val portfolioRepo = FakePortfolioRepository()
    private val marketRepo = FakeMarketRepository()

    private val getPortfolioUseCase = GetPortfolioUseCase(portfolioRepo)
    private val getMultipleQuotesUseCase = GetMultipleQuotesUseCase(marketRepo)
    private val getDividendInfoUseCase = GetDividendInfoUseCase(marketRepo)

    private val sut = GetPortfolioWithQuotesUseCase(
        getPortfolioUseCase = getPortfolioUseCase,
        getMultipleQuotesUseCase = getMultipleQuotesUseCase,
        getDividendInfoUseCase = getDividendInfoUseCase,
    )

    @Test
    fun `SHOULD emit empty list WHEN portfolio is empty GIVEN no holdings`() = runTest {
        // GIVEN — empty portfolio

        // WHEN
        val result = sut().first()

        // THEN
        assertTrue(result.isEmpty())
    }

    @Test
    fun `SHOULD emit SecurityHolding with quote WHEN holding exists GIVEN valid quote`() = runTest {
        // GIVEN
        val holding = FakePortfolioRepository.holding(tickerId = "AAPL", shares = 10.0, purchasePrice = 100.0)
        portfolioRepo.setHoldings(listOf(holding))
        val quote = FakeMarketRepository.quote(ticker = "AAPL", price = 150.0)
        marketRepo.setQuote("AAPL", quote)
        marketRepo.setDividendInfo("AAPL", FakeMarketRepository.dividendInfo())

        // WHEN
        val result = sut().first()

        // THEN
        assertEquals(1, result.size)
        val securityHolding = result.first()
        assertEquals("AAPL", securityHolding.holding.tickerId)
        assertEquals(quote, securityHolding.quote)
    }

    @Test
    fun `SHOULD compute totalGainPercent correctly WHEN price increased GIVEN purchase and current price`() = runTest {
        // GIVEN — bought at 100, now 150 → +50%
        val holding = FakePortfolioRepository.holding(tickerId = "AAPL", shares = 10.0, purchasePrice = 100.0)
        portfolioRepo.setHoldings(listOf(holding))
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 150.0))
        marketRepo.setDividendInfo("AAPL", FakeMarketRepository.dividendInfo())

        // WHEN
        val result = sut().first()

        // THEN
        assertEquals(50.0, result.first().totalGainPercent, 0.0001)
    }

    @Test
    fun `SHOULD set dividendInfo to null WHEN market returns error GIVEN fetch failure`() = runTest {
        // GIVEN
        val holding = FakePortfolioRepository.holding(tickerId = "AAPL")
        portfolioRepo.setHoldings(listOf(holding))
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote())
        marketRepo.setDividendError("AAPL", RuntimeException("No dividend data"))

        // WHEN
        val result = sut().first()

        // THEN
        assertNull(result.first().dividendInfo)
    }

    @Test
    fun `SHOULD exclude holding WHEN quote fetch fails GIVEN unavailable ticker`() = runTest {
        // GIVEN — quote not registered → getMultipleQuotes returns empty
        val holding = FakePortfolioRepository.holding(tickerId = "UNKNOWN")
        portfolioRepo.setHoldings(listOf(holding))
        // No quote set for UNKNOWN — FakeMarketRepository.getMultipleQuotes returns empty list

        // WHEN
        val result = sut().first()

        // THEN — no SecurityHolding emitted because quote is missing
        assertTrue(result.isEmpty())
    }

    @Test
    fun `SHOULD emit multiple SecurityHoldings WHEN portfolio has several holdings GIVEN all quotes available`() = runTest {
        // GIVEN
        portfolioRepo.setHoldings(
            listOf(
                FakePortfolioRepository.holding(id = "h1", tickerId = "AAPL", shares = 5.0, purchasePrice = 100.0),
                FakePortfolioRepository.holding(id = "h2", tickerId = "MSFT", shares = 3.0, purchasePrice = 200.0),
            ),
        )
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 150.0))
        marketRepo.setQuote("MSFT", FakeMarketRepository.quote(ticker = "MSFT", price = 300.0))
        marketRepo.setDividendInfo("AAPL", FakeMarketRepository.dividendInfo("AAPL"))
        marketRepo.setDividendInfo("MSFT", FakeMarketRepository.dividendInfo("MSFT"))

        // WHEN
        val result = sut().first()

        // THEN
        assertEquals(2, result.size)
        val tickers = result.map { it.holding.tickerId }
        assertTrue("AAPL" in tickers)
        assertTrue("MSFT" in tickers)
    }
}
