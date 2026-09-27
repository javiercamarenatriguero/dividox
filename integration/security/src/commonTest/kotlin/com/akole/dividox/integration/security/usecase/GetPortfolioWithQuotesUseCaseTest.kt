package com.akole.dividox.integration.security.usecase

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates
import com.akole.dividox.common.currency.domain.repository.ExchangeRateRepository
import com.akole.dividox.common.currency.domain.usecase.GetExchangeRatesUseCase
import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.integration.security.FakeMarketRepository
import com.akole.dividox.integration.security.FakePortfolioRepository
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetPortfolioWithQuotesUseCaseTest {

    private val portfolioRepo = FakePortfolioRepository()
    private val marketRepo = FakeMarketRepository()

    private val getPortfolioUseCase = GetPortfolioUseCase(portfolioRepo)
    private val getMultipleQuotesUseCase = GetMultipleQuotesUseCase(marketRepo)
    private val getDividendInfoUseCase = GetDividendInfoUseCase(marketRepo)

    // Identity rates: all currencies convert 1:1. USD↔USD short-circuits anyway.
    private val currencyConverter = CurrencyConverter(
        GetExchangeRatesUseCase(
            object : ExchangeRateRepository {
                override suspend fun getExchangeRates(base: Currency): Result<ExchangeRates> =
                    Result.success(
                        ExchangeRates(
                            base = base,
                            date = LocalDate(2024, 1, 1),
                            rates = Currency.entries.associateWith { 1.0 },
                        )
                    )
            }
        )
    )

    private val sut = GetPortfolioWithQuotesUseCase(
        getPortfolioUseCase = getPortfolioUseCase,
        getMultipleQuotesUseCase = getMultipleQuotesUseCase,
        getDividendInfoUseCase = getDividendInfoUseCase,
        currencyConverter = currencyConverter,
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
        // GIVEN — two holdings, only AAPL has a quote; UNKNOWN is excluded from the result
        portfolioRepo.setHoldings(
            listOf(
                FakePortfolioRepository.holding(id = "h1", tickerId = "AAPL", shares = 10.0, purchasePrice = 100.0),
                FakePortfolioRepository.holding(id = "h2", tickerId = "UNKNOWN", shares = 5.0, purchasePrice = 50.0),
            ),
        )
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 150.0))
        marketRepo.setDividendInfo("AAPL", FakeMarketRepository.dividendInfo("AAPL"))
        // No quote set for UNKNOWN → excluded by filter in GetPortfolioWithQuotesUseCase

        // WHEN
        val result = sut().first()

        // THEN — only AAPL holding emitted; UNKNOWN is excluded because its quote is missing
        assertEquals(1, result.size)
        assertEquals("AAPL", result.first().holding.tickerId)
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

    @Test
    fun `SHOULD emit stale then fresh then enriched WHEN cache has expired quotes GIVEN holding`() = runTest {
        // GIVEN
        val holding = FakePortfolioRepository.holding(tickerId = "AAPL", shares = 10.0, purchasePrice = 100.0)
        portfolioRepo.setHoldings(listOf(holding))
        marketRepo.setCachedQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 120.0))
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 150.0))
        marketRepo.setDividendInfo("AAPL", FakeMarketRepository.dividendInfo())

        // WHEN
        val emissions = sut().take(3).toList()

        // THEN
        assertEquals(120.0, emissions[0].first().quote.price)
        assertFalse(emissions[0].first().isDividendInfoResolved)
        assertEquals(150.0, emissions[1].first().quote.price)
        assertFalse(emissions[1].first().isDividendInfoResolved)
        assertTrue(emissions[2].first().isDividendInfoResolved)
        assertTrue(emissions[2].first().dividendInfo != null)
    }

    @Test
    fun `SHOULD keep dividend info resolved WHEN portfolio re-emits GIVEN dividends already fetched`() = runTest {
        // GIVEN
        val holding = FakePortfolioRepository.holding(tickerId = "AAPL", shares = 10.0, purchasePrice = 100.0)
        portfolioRepo.setHoldings(listOf(holding))
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 150.0))
        marketRepo.setDividendInfo("AAPL", FakeMarketRepository.dividendInfo())

        // WHEN — enriched emission, then a new Firestore snapshot (e.g. cache → server)
        val emissions = sut()
            .onEach { if (it.firstOrNull()?.isDividendInfoResolved == true) portfolioRepo.setHoldings(listOf(holding.copy(shares = 11.0))) }
            .take(3).toList()

        // THEN — the re-emission carries the dividend info instead of dropping back to pending
        assertEquals(11.0, emissions[2].first().holding.shares)
        assertTrue(emissions[2].first().isDividendInfoResolved)
        assertTrue(emissions[2].first().dividendInfo != null)
    }

    @Test
    fun `SHOULD emit fresh then enriched WHEN cache is empty GIVEN holding`() = runTest {
        // GIVEN
        val holding = FakePortfolioRepository.holding(tickerId = "AAPL", shares = 10.0, purchasePrice = 100.0)
        portfolioRepo.setHoldings(listOf(holding))
        marketRepo.setQuote("AAPL", FakeMarketRepository.quote(ticker = "AAPL", price = 150.0))
        marketRepo.setDividendInfo("AAPL", FakeMarketRepository.dividendInfo())

        // WHEN
        val emissions = sut().take(2).toList()

        // THEN
        assertEquals(150.0, emissions[0].first().quote.price)
        assertNull(emissions[0].first().dividendInfo)
        assertTrue(emissions[1].first().dividendInfo != null)
    }
}
