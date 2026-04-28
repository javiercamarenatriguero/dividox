package com.akole.dividox.component.market.data.api

import com.akole.dividox.component.market.data.dto.ChartResponseDto
import com.akole.dividox.component.market.data.dto.SearchResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * Low-level HTTP client for Yahoo Finance public endpoints.
 *
 * All endpoints are unauthenticated and subject to rate limiting (HTTP 429).
 * Only `v8/finance/chart` is reliably accessible without a crumb/session cookie.
 */
internal class YahooFinanceApi(private val client: HttpClient) {

    /**
     * Fetches OHLCV chart data for [ticker].
     *
     * @param ticker Yahoo Finance symbol (e.g. "AAPL", "JNJ").
     * @param range  Historical range string accepted by Yahoo (e.g. "1d", "5d", "1mo", "1y", "max").
     * @param interval Candle interval (e.g. "5m", "1h", "1d", "1wk", "1mo").
     */
    suspend fun getChart(ticker: String, range: String = "1d", interval: String = "1d"): ChartResponseDto =
        client.get("https://query1.finance.yahoo.com/v8/finance/chart/$ticker") {
            parameter("range", range)
            parameter("interval", interval)
        }.body()

    /**
     * Fetches chart data including historical dividend events for [ticker].
     *
     * Uses a 5-year range with quarterly candles and `events=dividends`.
     * The response [ChartResponseDto] will contain a `dividends` map under `events`
     * keyed by Unix timestamp strings.
     *
     * @param ticker Yahoo Finance symbol.
     * @param range  Historical range (default 5y to capture enough dividend history for CAGR).
     */
    suspend fun getChartWithEvents(ticker: String, range: String = "5y"): ChartResponseDto =
        client.get("https://query1.finance.yahoo.com/v8/finance/chart/$ticker") {
            parameter("range", range)
            parameter("interval", "3mo")
            parameter("events", "dividends")
        }.body()

    /**
     * Searches for securities matching [query].
     *
     * Returns up to 10 equity quotes and 0 news items.
     * Results are minimal stubs — price data is not included; a follow-up [getChart] call is needed.
     *
     * @param query Free-text search string (ticker or company name).
     */
    suspend fun search(query: String): SearchResponseDto =
        client.get("https://query1.finance.yahoo.com/v1/finance/search") {
            parameter("q", query)
            parameter("quotesCount", 10)
            parameter("newsCount", 0)
        }.body()
}
