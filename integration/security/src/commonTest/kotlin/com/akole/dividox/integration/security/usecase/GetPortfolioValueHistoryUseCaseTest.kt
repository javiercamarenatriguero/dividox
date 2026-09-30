package com.akole.dividox.integration.security.usecase

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates
import com.akole.dividox.common.currency.domain.repository.ExchangeRateRepository
import com.akole.dividox.common.currency.domain.usecase.GetExchangeRatesUseCase
import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
import com.akole.dividox.integration.security.FakeMarketRepository
import com.akole.dividox.integration.security.FakePortfolioRepository
import com.akole.dividox.integration.security.domain.model.PortfolioValuePoint
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import com.akole.dividox.integration.security.domain.usecase.GetPortfolioValueHistoryUseCase
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetPortfolioValueHistoryUseCaseTest {

    private val marketRepo = FakeMarketRepository()
    private val today = LocalDate(2024, 1, 10)

    private fun sut(rates: Map<Currency, Map<Currency, Double>> = mapOf(Currency.USD to mapOf(Currency.EUR to 0.5))) =
        GetPortfolioValueHistoryUseCase(
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
            today = { today },
        )

    private fun sh(ticker: String, currency: String, price: Double, shares: Double = 10.0) = SecurityHolding(
        holding = FakePortfolioRepository.holding(id = ticker, tickerId = ticker, shares = shares),
        quote = FakeMarketRepository.quote(ticker = ticker, price = price, change = 0.0, currency = currency),
        dividendInfo = null,
        totalGainPercent = 0.0,
    )

    private fun point(day: Int, close: Double) =
        PricePoint(LocalDate(2024, 1, day).atStartOfDayIn(TimeZone.UTC), close)

    @Test
    fun `SHOULD value current shares at historical closes WHEN history is available GIVEN single holding`() = runTest {
        // GIVEN
        marketRepo.setPriceHistory("SAN.MC", listOf(point(1, 5.0), point(2, 6.0)))

        // WHEN
        val result = sut()(listOf(sh("SAN.MC", "EUR", price = 7.0)), ChartPeriod.ONE_MONTH, Currency.EUR)

        // THEN — last point is today's live value
        assertEquals(
            listOf(
                PortfolioValuePoint(LocalDate(2024, 1, 1), 50.0),
                PortfolioValuePoint(LocalDate(2024, 1, 2), 60.0),
                PortfolioValuePoint(today, 70.0),
            ),
            result,
        )
    }

    @Test
    fun `SHOULD align dates and carry closes WHEN series have gaps GIVEN mixed currencies`() = runTest {
        // GIVEN — AAPL (USD) has no close on day 1, SAN (EUR) none on day 2
        marketRepo.setPriceHistory("SAN.MC", listOf(point(1, 5.0), point(3, 6.0)))
        marketRepo.setPriceHistory("AAPL", listOf(point(2, 20.0), point(3, 30.0)))
        val holdings = listOf(sh("SAN.MC", "EUR", price = 6.0), sh("AAPL", "USD", price = 30.0))

        // WHEN
        val result = sut()(holdings, ChartPeriod.ONE_MONTH, Currency.EUR)!!

        // THEN — day1: 50 + 20*10*0.5 (backfill), day2: 50 (carry) + 100, day3: 60 + 150
        assertEquals(listOf(150.0, 150.0, 210.0, 210.0), result.map { it.value })
    }

    @Test
    fun `SHOULD return null WHEN exchange rate is unavailable GIVEN foreign currency holding`() = runTest {
        // GIVEN
        val holdings = listOf(sh("AAPL", "USD", price = 30.0))

        // WHEN
        val result = sut(rates = emptyMap())(holdings, ChartPeriod.ONE_YEAR, Currency.EUR)

        // THEN
        assertNull(result)
    }

    @Test
    fun `SHOULD return empty list WHEN portfolio is empty GIVEN any period`() = runTest {
        // WHEN
        val result = sut()(emptyList(), ChartPeriod.ONE_YEAR, Currency.EUR)

        // THEN
        assertTrue(result!!.isEmpty())
    }
}
