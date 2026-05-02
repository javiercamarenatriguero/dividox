package com.akole.dividox.common.currency

import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates
import com.akole.dividox.common.currency.domain.repository.ExchangeRateRepository
import com.akole.dividox.common.currency.domain.usecase.GetExchangeRatesUseCase
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CurrencyConverterTest {

    private val sampleDate = LocalDate(2025, 1, 15)
    private val sampleRates = ExchangeRates(
        base = Currency.EUR,
        date = sampleDate,
        rates = mapOf(
            Currency.USD to 1.05,
            Currency.GBP to 0.85,
        ),
    )

    private fun converterWith(result: Result<ExchangeRates>): CurrencyConverter {
        val repository = object : ExchangeRateRepository {
            override suspend fun getExchangeRates(base: Currency) = result
        }
        return CurrencyConverter(GetExchangeRatesUseCase(repository))
    }

    @Test
    fun `GIVEN same currency WHEN convert THEN returns original amount`() = runTest {
        // GIVEN
        val converter = converterWith(Result.success(sampleRates))
        // WHEN
        val result = converter.convert(100.0, Currency.EUR, Currency.EUR)
        // THEN
        assertEquals(100.0, result.getOrThrow())
    }

    @Test
    fun `GIVEN EUR to USD WHEN convert THEN returns amount times rate`() = runTest {
        // GIVEN
        val converter = converterWith(Result.success(sampleRates))
        // WHEN
        val result = converter.convert(100.0, Currency.EUR, Currency.USD)
        // THEN
        assertEquals(105.0, result.getOrThrow())
    }

    @Test
    fun `GIVEN EUR to GBP WHEN convert THEN returns amount times rate`() = runTest {
        // GIVEN
        val converter = converterWith(Result.success(sampleRates))
        // WHEN
        val result = converter.convert(200.0, Currency.EUR, Currency.GBP)
        // THEN
        assertEquals(170.0, result.getOrThrow())
    }

    @Test
    fun `GIVEN unknown target currency WHEN convert THEN returns failure`() = runTest {
        // GIVEN
        val converter = converterWith(Result.success(sampleRates))
        // WHEN
        val result = converter.convert(100.0, Currency.EUR, Currency.JPY)
        // THEN
        assertTrue(result.isFailure)
    }

    @Test
    fun `GIVEN network error WHEN convert THEN propagates failure`() = runTest {
        // GIVEN
        val error = RuntimeException("Network error")
        val converter = converterWith(Result.failure(error))
        // WHEN
        val result = converter.convert(100.0, Currency.EUR, Currency.USD)
        // THEN
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
}
