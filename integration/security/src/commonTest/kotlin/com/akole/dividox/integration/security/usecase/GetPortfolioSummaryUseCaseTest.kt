package com.akole.dividox.integration.security.usecase

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates
import com.akole.dividox.common.currency.domain.repository.ExchangeRateRepository
import com.akole.dividox.common.currency.domain.usecase.GetExchangeRatesUseCase
import kotlinx.datetime.LocalDate
import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.integration.security.FakeMarketRepository
import com.akole.dividox.integration.security.FakePortfolioRepository
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioSummaryUseCase
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioWithQuotesUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetPortfolioSummaryUseCaseTest {

    private val portfolioRepo = FakePortfolioRepository()
    private val marketRepo = FakeMarketRepository()

    // Identity rates — returns 1:1 for all pairs. USD→USD short-circuits before calling this.
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

    private val getPortfolioWithQuotesUseCase = GetPortfolioWithQuotesUseCase(
        getPortfolioUseCase = GetPortfolioUseCase(portfolioRepo),
        getMultipleQuotesUseCase = GetMultipleQuotesUseCase(marketRepo),
        getDividendInfoUseCase = GetDividendInfoUseCase(marketRepo),
        currencyConverter = currencyConverter,
    )

    private val sut = GetPortfolioSummaryUseCase(getPortfolioWithQuotesUseCase, currencyConverter)

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
        val summary = sut().first { it.isDividendDataResolved }

        // THEN
        assertEquals(9.6, summary.dividendsCollected, 0.0001)
    }

    // ─── summarize(target) ────────────────────────────────────────────────────

    // Deliberately non-reciprocal rates (EUR→USD 1.10, USD→EUR 0.90): a USD round trip would
    // turn 1 EUR into 0.99 EUR, so any USD pivot shows up as an inexact result.
    private fun converterWith(rates: Map<Currency, Map<Currency, Double>>) = CurrencyConverter(
        GetExchangeRatesUseCase(
            object : ExchangeRateRepository {
                override suspend fun getExchangeRates(base: Currency): Result<ExchangeRates> =
                    rates[base]?.let { Result.success(ExchangeRates(base, LocalDate(2024, 1, 1), it)) }
                        ?: Result.failure(IllegalStateException("No rates for $base"))
            },
        ),
    )

    private fun securityHolding(quoteCurrency: String, purchaseCurrency: Currency) = SecurityHolding(
        holding = FakePortfolioRepository.holding(tickerId = "SAN.MC", shares = 10.0, purchasePrice = 4.0, currency = purchaseCurrency),
        quote = FakeMarketRepository.quote(ticker = "SAN.MC", price = 5.0, currency = quoteCurrency),
        dividendInfo = null,
        totalGainPercent = 0.0,
    )

    @Test
    fun `SHOULD compute exact value in display currency WHEN holding is in same currency GIVEN non-reciprocal rates`() = runTest {
        // GIVEN
        val converter = converterWith(
            mapOf(Currency.EUR to mapOf(Currency.USD to 1.10), Currency.USD to mapOf(Currency.EUR to 0.90)),
        )
        val sut = GetPortfolioSummaryUseCase(getPortfolioWithQuotesUseCase, converter)

        // WHEN
        val summary = sut.summarize(listOf(securityHolding("EUR", Currency.EUR)), Currency.EUR)

        // THEN — 10 × 5.00 EUR, no USD round trip
        assertEquals(50.0, summary?.totalValue)
        assertEquals(10.0, summary?.totalGain)
    }

    @Test
    fun `SHOULD return null WHEN exchange rate is unavailable GIVEN holding in foreign currency`() = runTest {
        // GIVEN — no rates at all
        val sut = GetPortfolioSummaryUseCase(getPortfolioWithQuotesUseCase, converterWith(emptyMap()))

        // WHEN
        val summary = sut.summarize(listOf(securityHolding("USD", Currency.USD)), Currency.EUR)

        // THEN — no mixed-currency sum is produced
        assertNull(summary)
    }
}
