package com.akole.dividox.common.currency.data.datasource

import com.akole.dividox.common.currency.data.dto.ExchangeRateResponseDto
import com.akole.dividox.common.currency.domain.model.Currency
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FrankfurterExchangeRateDataSourceTest {

    private val sampleResponse = """{
        "amount": 1.0,
        "base": "EUR",
        "date": "2025-01-15",
        "rates": {
            "USD": 1.05,
            "GBP": 0.85,
            "JPY": 160.5
        }
    }"""

    private fun buildClient(responseBody: String, status: HttpStatusCode = HttpStatusCode.OK): HttpClient {
        val engine = MockEngine {
            respond(
                content = responseBody,
                status = status,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
        }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    @Test
    fun `GIVEN valid response WHEN getExchangeRates THEN maps to domain model`() = runTest {
        // GIVEN
        val dataSource = FrankfurterExchangeRateDataSource(buildClient(sampleResponse))
        // WHEN
        val result = dataSource.getExchangeRates(Currency.EUR)
        // THEN
        assertTrue(result.isSuccess)
        val rates = result.getOrThrow()
        assertEquals(Currency.EUR, rates.base)
        assertEquals(LocalDate(2025, 1, 15), rates.date)
        assertEquals(1.05, rates.rates[Currency.USD])
        assertEquals(0.85, rates.rates[Currency.GBP])
        assertEquals(160.5, rates.rates[Currency.JPY])
    }

    @Test
    fun `GIVEN response with unknown currency codes WHEN getExchangeRates THEN ignores unknown codes`() = runTest {
        // GIVEN
        val responseWithUnknown = """{
            "amount": 1.0,
            "base": "EUR",
            "date": "2025-01-15",
            "rates": {
                "USD": 1.05,
                "XYZ": 99.99
            }
        }"""
        val dataSource = FrankfurterExchangeRateDataSource(buildClient(responseWithUnknown))
        // WHEN
        val result = dataSource.getExchangeRates(Currency.EUR)
        // THEN
        assertTrue(result.isSuccess)
        val rates = result.getOrThrow()
        assertEquals(1.05, rates.rates[Currency.USD])
        assertEquals(1, rates.rates.size) // XYZ is dropped
    }

    @Test
    fun `GIVEN network error WHEN getExchangeRates THEN returns failure`() = runTest {
        // GIVEN
        val engine = MockEngine { throw RuntimeException("Network failure") }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json() }
        }
        val dataSource = FrankfurterExchangeRateDataSource(client)
        // WHEN
        val result = dataSource.getExchangeRates(Currency.EUR)
        // THEN
        assertTrue(result.isFailure)
    }
}
