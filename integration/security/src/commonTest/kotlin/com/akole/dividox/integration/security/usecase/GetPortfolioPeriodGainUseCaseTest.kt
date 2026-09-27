package com.akole.dividox.integration.security.usecase

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates
import com.akole.dividox.common.currency.domain.repository.ExchangeRateRepository
import com.akole.dividox.common.currency.domain.usecase.GetExchangeRatesUseCase
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
import com.akole.dividox.integration.security.FakeMarketRepository
import com.akole.dividox.integration.security.FakePortfolioRepository
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioPeriodGainUseCase
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetPortfolioPeriodGainUseCaseTest {

    private val marketRepo = FakeMarketRepository()

    // USD→EUR 0.5 (exaggerated so a missing conversion is obvious).
    private fun sut(rates: Map<Currency, Map<Currency, Double>>) = GetPortfolioPeriodGainUseCase(
        getPriceHistory = GetPriceHistoryUseCase(marketRepo),
        currencyConverter = CurrencyConverter(
            GetExchangeRatesUseCase(
                object : ExchangeRateRepository {
                    override suspend fun getExchangeRates(base: Currency): Result<ExchangeRates> =
                        rates[base]?.let { Result.success(ExchangeRates(base, LocalDate(2024, 1, 1), it)) }
                            ?: Result.failure(IllegalStateException("No rates for $base"))
                },
            ),
        ),
    )

    private fun sh(ticker: String, currency: String, price: Double, change: Double) = SecurityHolding(
        holding = FakePortfolioRepository.holding(id = ticker, tickerId = ticker, shares = 10.0),
        quote = FakeMarketRepository.quote(ticker = ticker, price = price, change = change, currency = currency),
        dividendInfo = null,
        totalGainPercent = 0.0,
    )

    @Test
    fun `SHOULD convert each holding before summing WHEN portfolio mixes currencies GIVEN ONE_DAY`() = runTest {
        // GIVEN — EUR stock +1 EUR/share, USD stock +2 USD/share (= +1 EUR/share)
        val holdings = listOf(sh("SAN.MC", "EUR", price = 11.0, change = 1.0), sh("AAPL", "USD", price = 22.0, change = 2.0))

        // WHEN
        val result = sut(mapOf(Currency.USD to mapOf(Currency.EUR to 0.5)))(holdings, ChartPeriod.ONE_DAY, Currency.EUR)

        // THEN — 10 EUR + 10 EUR gain over a 100 EUR + 100 EUR base
        assertEquals(20.0, result?.first)
        assertEquals(10.0, result?.second)
    }

    @Test
    fun `SHOULD return null WHEN exchange rate is unavailable GIVEN foreign currency holding`() = runTest {
        // GIVEN
        val holdings = listOf(sh("AAPL", "USD", price = 22.0, change = 2.0))

        // WHEN
        val result = sut(emptyMap())(holdings, ChartPeriod.ONE_DAY, Currency.EUR)

        // THEN
        assertNull(result)
    }
}
