package com.akole.dividox.component.market.data.api

import com.akole.dividox.component.market.data.dto.BatchQuoteResponseDto
import com.akole.dividox.component.market.data.dto.ChartResponseDto
import com.akole.dividox.component.market.data.dto.QuoteSummaryResponseDto
import com.akole.dividox.component.market.data.dto.SearchResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class YahooFinanceApi(private val client: HttpClient) {

    suspend fun getChart(ticker: String, range: String = "1d", interval: String = "1d"): ChartResponseDto =
        client.get("https://query1.finance.yahoo.com/v8/finance/chart/$ticker") {
            parameter("range", range)
            parameter("interval", interval)
        }.body()

    suspend fun getChartWithEvents(ticker: String, range: String = "5y"): ChartResponseDto =
        client.get("https://query1.finance.yahoo.com/v8/finance/chart/$ticker") {
            parameter("range", range)
            parameter("interval", "3mo")
            parameter("events", "dividends")
        }.body()

    suspend fun getQuoteSummary(ticker: String): QuoteSummaryResponseDto =
        client.get("https://query2.finance.yahoo.com/v10/finance/quoteSummary/$ticker") {
            parameter("modules", "summaryDetail,assetProfile")
        }.body()

    suspend fun getBatchQuotes(tickers: List<String>): BatchQuoteResponseDto =
        client.get("https://query1.finance.yahoo.com/v7/finance/quote") {
            parameter("symbols", tickers.joinToString(","))
        }.body()

    suspend fun search(query: String): SearchResponseDto =
        client.get("https://query1.finance.yahoo.com/v1/finance/search") {
            parameter("q", query)
            parameter("quotesCount", 10)
            parameter("newsCount", 0)
        }.body()
}
