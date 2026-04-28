package com.akole.dividox.component.market.data.repository

import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.MarketError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarketRepositoryImplTest {

    private val dispatcher = StandardTestDispatcher()

    private fun buildRepoWithResponses(vararg responses: Pair<String, String>): MarketRepositoryImpl {
        val queue = responses.toMutableList()
        val engine = MockEngine {
            val (_, body) = queue.removeFirst()
            respond(body, HttpStatusCode.OK, headersOf("Content-Type", ContentType.Application.Json.toString()))
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        return MarketRepositoryImpl(client, dispatcher)
    }

    // ── getStockQuote ─────────────────────────────────────────────────────────

    @Test
    fun `SHOULD return StockQuote WHEN chart endpoint responds GIVEN valid ticker`() = runTest(dispatcher) {
        // GIVEN
        val repo = buildRepoWithResponses("chart" to CHART_JSON_AAPL)

        // WHEN
        val result = repo.getStockQuote("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals("AAPL", result.getOrNull()?.ticker)
        assertEquals(150.0, result.getOrNull()?.price)
    }

    @Test
    fun `SHOULD compute change correctly WHEN previous close differs GIVEN chart response`() = runTest(dispatcher) {
        // GIVEN
        val repo = buildRepoWithResponses("chart" to CHART_JSON_AAPL)

        // WHEN
        val quote = repo.getStockQuote("AAPL").getOrThrow()

        // THEN
        // price=150.0, chartPreviousClose=148.5 → change=1.5
        assertEquals(1.5, quote.change, 0.001)
        assertTrue(quote.changePercent > 0.0)
    }

    @Test
    fun `SHOULD return RateLimited WHEN API returns 429 GIVEN throttled request`() = runTest(dispatcher) {
        // GIVEN
        val engine = MockEngine {
            respond("Too Many Requests", HttpStatusCode.TooManyRequests)
        }
        val client = HttpClient(engine) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
        val repo = MarketRepositoryImpl(client, dispatcher)

        // WHEN
        val result = repo.getStockQuote("AAPL")

        // THEN
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is MarketError.RateLimited)
    }

    @Test
    fun `SHOULD return from cache WHEN called twice GIVEN single network response`() = runTest(dispatcher) {
        // GIVEN
        // Only one response queued — second call must hit cache
        val repo = buildRepoWithResponses("chart" to CHART_JSON_AAPL)

        // WHEN
        val first = repo.getStockQuote("AAPL")
        val second = repo.getStockQuote("AAPL")

        // THEN
        assertTrue(first.isSuccess)
        assertTrue(second.isSuccess)
        assertEquals(first.getOrNull()?.price, second.getOrNull()?.price)
    }

    // ── getMultipleQuotes ─────────────────────────────────────────────────────

    @Test
    fun `SHOULD return quotes for all tickers WHEN parallel chart calls succeed GIVEN two tickers`() = runTest(dispatcher) {
        // GIVEN
        val repo = buildRepoWithResponses(
            "aapl" to CHART_JSON_AAPL,
            "msft" to CHART_JSON_MSFT,
        )

        // WHEN
        val result = repo.getMultipleQuotes(listOf("AAPL", "MSFT"))

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }

    // ── getDividendInfo ───────────────────────────────────────────────────────

    @Test
    fun `SHOULD return DividendInfo WHEN chart-with-events responds GIVEN dividend-paying ticker`() = runTest(dispatcher) {
        // GIVEN
        val repo = buildRepoWithResponses("events" to CHART_WITH_EVENTS_JSON)

        // WHEN
        val result = repo.getDividendInfo("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        val info = result.getOrNull()!!
        assertEquals("AAPL", info.ticker)
        assertTrue(info.annualPayout > 0.0)
        assertTrue(info.yield > 0.0)
    }

    @Test
    fun `SHOULD return zero yield WHEN ticker pays no dividends GIVEN events map is empty`() = runTest(dispatcher) {
        // GIVEN
        val repo = buildRepoWithResponses("events" to CHART_WITH_EVENTS_EMPTY_JSON)

        // WHEN
        val result = repo.getDividendInfo("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(0.0, result.getOrNull()?.annualPayout)
    }

    // ── getCompanyInfo ────────────────────────────────────────────────────────

    @Test
    fun `SHOULD return CompanyInfo with longName WHEN chart-with-events has longName GIVEN valid ticker`() = runTest(dispatcher) {
        // GIVEN
        val repo = buildRepoWithResponses("events" to CHART_WITH_EVENTS_JSON)

        // WHEN
        val result = repo.getCompanyInfo("AAPL")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals("Apple Inc.", result.getOrNull()?.name)
        assertEquals("NMS", result.getOrNull()?.exchange)
    }

    // ── getPriceHistory ───────────────────────────────────────────────────────

    @Test
    fun `SHOULD emit price points WHEN chart has timestamps and closes GIVEN ONE_MONTH period`() = runTest(dispatcher) {
        // GIVEN
        val repo = buildRepoWithResponses("chart" to CHART_JSON_AAPL)

        // WHEN
        val points = repo.getPriceHistory("AAPL", ChartPeriod.ONE_MONTH).first()

        // THEN
        assertEquals(2, points.size)
        assertEquals(150.0, points.last().close)
    }

    // ── searchSecurities ──────────────────────────────────────────────────────

    @Test
    fun `SHOULD return equity stubs WHEN search responds GIVEN query string`() = runTest(dispatcher) {
        // GIVEN
        val repo = buildRepoWithResponses("search" to SEARCH_JSON)

        // WHEN
        val result = repo.searchSecurities("apple")

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals("AAPL", result.getOrNull()?.first()?.ticker)
    }

    // ── JSON fixtures ─────────────────────────────────────────────────────────

    companion object {
        val CHART_JSON_AAPL = """
            {
              "chart": {
                "result": [{
                  "meta": {
                    "symbol": "AAPL",
                    "regularMarketPrice": 150.0,
                    "chartPreviousClose": 148.5,
                    "currency": "USD",
                    "regularMarketTime": 1700000000,
                    "exchangeName": "NMS"
                  },
                  "timestamp": [1700000000, 1700086400],
                  "indicators": {
                    "quote": [{ "close": [148.0, 150.0] }]
                  }
                }],
                "error": null
              }
            }
        """.trimIndent()

        val CHART_JSON_MSFT = """
            {
              "chart": {
                "result": [{
                  "meta": {
                    "symbol": "MSFT",
                    "regularMarketPrice": 420.0,
                    "chartPreviousClose": 418.0,
                    "currency": "USD",
                    "regularMarketTime": 1700000000,
                    "exchangeName": "NMS"
                  },
                  "timestamp": [1700000000],
                  "indicators": {
                    "quote": [{ "close": [420.0] }]
                  }
                }],
                "error": null
              }
            }
        """.trimIndent()

        val CHART_WITH_EVENTS_JSON = """
            {
              "chart": {
                "result": [{
                  "meta": {
                    "symbol": "AAPL",
                    "longName": "Apple Inc.",
                    "regularMarketPrice": 150.0,
                    "chartPreviousClose": 148.5,
                    "currency": "USD",
                    "regularMarketTime": 1700000000,
                    "exchangeName": "NMS"
                  },
                  "events": {
                    "dividends": {
                      "1640000000": { "amount": 0.22, "date": 1640000000 },
                      "1648000000": { "amount": 0.23, "date": 1648000000 },
                      "1656000000": { "amount": 0.23, "date": 1656000000 },
                      "1664000000": { "amount": 0.24, "date": 1664000000 }
                    }
                  }
                }],
                "error": null
              }
            }
        """.trimIndent()

        val CHART_WITH_EVENTS_EMPTY_JSON = """
            {
              "chart": {
                "result": [{
                  "meta": {
                    "symbol": "AAPL",
                    "longName": "Apple Inc.",
                    "regularMarketPrice": 150.0,
                    "currency": "USD",
                    "regularMarketTime": 1700000000,
                    "exchangeName": "NMS"
                  },
                  "events": {}
                }],
                "error": null
              }
            }
        """.trimIndent()

        val SEARCH_JSON = """
            {
              "quotes": [
                { "symbol": "AAPL", "quoteType": "EQUITY" }
              ]
            }
        """.trimIndent()
    }
}
