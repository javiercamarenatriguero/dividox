package com.akole.dividox.integration.security.usecase

import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.usecase.GetCompanyInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.component.watchlist.domain.usecase.IsInWatchlistUseCase
import com.akole.dividox.integration.security.FakeMarketRepository
import com.akole.dividox.integration.security.FakePortfolioRepository
import com.akole.dividox.integration.security.FakeWatchlistRepository
import com.akole.dividox.integration.security.domain.usecase.GetSecurityDetailUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetSecurityDetailUseCaseTest {

    private val portfolioRepo = FakePortfolioRepository()
    private val watchlistRepo = FakeWatchlistRepository()
    private val marketRepo = FakeMarketRepository()

    private val sut = GetSecurityDetailUseCase(
        getStockQuoteUseCase = GetStockQuoteUseCase(marketRepo),
        getDividendInfoUseCase = GetDividendInfoUseCase(marketRepo),
        getCompanyInfoUseCase = GetCompanyInfoUseCase(marketRepo),
        getPriceHistoryUseCase = GetPriceHistoryUseCase(marketRepo),
        getPortfolioUseCase = GetPortfolioUseCase(portfolioRepo),
        isInWatchlistUseCase = IsInWatchlistUseCase(watchlistRepo),
    )

    @Test
    fun `SHOULD return SecurityDetail with quote WHEN ticker is valid GIVEN quote available`() = runTest {
        // GIVEN
        val ticker = "AAPL"
        marketRepo.setQuote(ticker, FakeMarketRepository.quote(ticker = ticker))
        marketRepo.setDividendInfo(ticker, FakeMarketRepository.dividendInfo(ticker = ticker))
        marketRepo.setCompanyInfo(ticker, FakeMarketRepository.companyInfo(ticker = ticker))

        // WHEN
        val detail = sut(ticker, ChartPeriod.ONE_MONTH).first()

        // THEN
        assertNotNull(detail)
        assertEquals(ticker, detail.ticker)
        assertEquals(ticker, detail.quote.ticker)
    }

    @Test
    fun `SHOULD set isInPortfolio to true WHEN ticker is held GIVEN matching holding`() = runTest {
        // GIVEN
        val ticker = "AAPL"
        portfolioRepo.setHoldings(listOf(FakePortfolioRepository.holding(tickerId = ticker)))
        marketRepo.setQuote(ticker, FakeMarketRepository.quote(ticker = ticker))
        marketRepo.setDividendError(ticker, RuntimeException())
        marketRepo.setCompanyError(ticker, RuntimeException())

        // WHEN
        val detail = sut(ticker, ChartPeriod.ONE_DAY).first()

        // THEN
        assertTrue(detail.isInPortfolio)
        assertNotNull(detail.holdingId)
    }

    @Test
    fun `SHOULD set isInPortfolio to false WHEN ticker not held GIVEN empty portfolio`() = runTest {
        // GIVEN
        val ticker = "AAPL"
        marketRepo.setQuote(ticker, FakeMarketRepository.quote(ticker = ticker))
        marketRepo.setDividendError(ticker, RuntimeException())
        marketRepo.setCompanyError(ticker, RuntimeException())

        // WHEN
        val detail = sut(ticker, ChartPeriod.ONE_DAY).first()

        // THEN
        assertFalse(detail.isInPortfolio)
        assertNull(detail.holdingId)
    }

    @Test
    fun `SHOULD set isInWatchlist to true WHEN ticker is watched GIVEN watchlist entry`() = runTest {
        // GIVEN
        val ticker = "AAPL"
        watchlistRepo.addToWatchlist(ticker)
        marketRepo.setQuote(ticker, FakeMarketRepository.quote(ticker = ticker))
        marketRepo.setDividendError(ticker, RuntimeException())
        marketRepo.setCompanyError(ticker, RuntimeException())

        // WHEN
        val detail = sut(ticker, ChartPeriod.ONE_DAY).first()

        // THEN
        assertTrue(detail.isInWatchlist)
    }

    @Test
    fun `SHOULD set dividendInfo to null WHEN dividend fetch fails GIVEN market error`() = runTest {
        // GIVEN
        val ticker = "AAPL"
        marketRepo.setQuote(ticker, FakeMarketRepository.quote())
        marketRepo.setDividendError(ticker, RuntimeException("No dividend"))
        marketRepo.setCompanyInfo(ticker, FakeMarketRepository.companyInfo())

        // WHEN
        val detail = sut(ticker, ChartPeriod.ONE_MONTH).first()

        // THEN
        assertNull(detail.dividendInfo)
    }

    @Test
    fun `SHOULD return empty priceHistory WHEN no history registered GIVEN default fake state`() = runTest {
        // GIVEN
        val ticker = "AAPL"
        marketRepo.setQuote(ticker, FakeMarketRepository.quote())
        marketRepo.setDividendError(ticker, RuntimeException())
        marketRepo.setCompanyError(ticker, RuntimeException())

        // WHEN
        val detail = sut(ticker, ChartPeriod.ONE_WEEK).first()

        // THEN
        assertTrue(detail.priceHistory.isEmpty())
    }
}
