package com.akole.dividox.integration.security.usecase

import com.akole.dividox.component.market.domain.usecase.GetCompanyInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.component.watchlist.domain.usecase.GetWatchlistUseCase
import com.akole.dividox.integration.security.FakeMarketRepository
import com.akole.dividox.integration.security.FakePortfolioRepository
import com.akole.dividox.integration.security.FakeWatchlistRepository
import com.akole.dividox.integration.security.domain.usecase.GetEnrichedWatchlistUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetEnrichedWatchlistUseCaseTest {

    private val watchlistRepo = FakeWatchlistRepository()
    private val portfolioRepo = FakePortfolioRepository()
    private val marketRepo = FakeMarketRepository()

    private val sut = GetEnrichedWatchlistUseCase(
        getWatchlistUseCase = GetWatchlistUseCase(watchlistRepo),
        getMultipleQuotesUseCase = GetMultipleQuotesUseCase(marketRepo),
        getCompanyInfoUseCase = GetCompanyInfoUseCase(marketRepo),
        getPortfolioUseCase = GetPortfolioUseCase(portfolioRepo),
    )

    @Test
    fun `SHOULD emit empty list WHEN watchlist is empty GIVEN no entries`() = runTest {
        // GIVEN — empty watchlist

        // WHEN
        val result = sut().first()

        // THEN
        assertTrue(result.isEmpty())
    }

    @Test
    fun `SHOULD enrich entry with quote and companyInfo WHEN market data available GIVEN watchlist entry`() = runTest {
        // GIVEN
        watchlistRepo.addToWatchlist("AAPL")
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL"))
        marketRepo.setCompanyInfo("AAPL", FakeMarketRepository.companyInfo(ticker = "AAPL"))

        // WHEN
        val result = sut().first()

        // THEN
        assertEquals(1, result.size)
        val entry = result.first()
        assertEquals("AAPL", entry.entry.tickerId)
        assertEquals("AAPL", entry.quote?.ticker)
        assertEquals("Apple Inc.", entry.companyInfo?.name)
    }

    @Test
    fun `SHOULD set quote to null WHEN quote unavailable GIVEN market fetch fails`() = runTest {
        // GIVEN — no quote registered for UNKNOWN
        watchlistRepo.addToWatchlist("UNKNOWN")

        // WHEN
        val result = sut().first()

        // THEN
        assertNull(result.first().quote)
    }

    @Test
    fun `SHOULD set companyInfo to null WHEN company info fetch fails GIVEN market error`() = runTest {
        // GIVEN
        watchlistRepo.addToWatchlist("AAPL")
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote())
        marketRepo.setCompanyError("AAPL", RuntimeException("company unavailable"))

        // WHEN
        val result = sut().first()

        // THEN
        assertNull(result.first().companyInfo)
    }

    @Test
    fun `SHOULD set isInPortfolio to true WHEN ticker is in portfolio GIVEN matching holding`() = runTest {
        // GIVEN
        watchlistRepo.addToWatchlist("AAPL")
        portfolioRepo.setHoldings(listOf(FakePortfolioRepository.holding(tickerId = "AAPL")))
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote())

        // WHEN
        val result = sut().first()

        // THEN
        assertTrue(result.first().isInPortfolio)
    }

    @Test
    fun `SHOULD set isInPortfolio to false WHEN ticker not in portfolio GIVEN no matching holding`() = runTest {
        // GIVEN
        watchlistRepo.addToWatchlist("AAPL")
        portfolioRepo.setHoldings(listOf(FakePortfolioRepository.holding(tickerId = "MSFT")))
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote())

        // WHEN
        val result = sut().first()

        // THEN
        assertFalse(result.first().isInPortfolio)
    }
}
