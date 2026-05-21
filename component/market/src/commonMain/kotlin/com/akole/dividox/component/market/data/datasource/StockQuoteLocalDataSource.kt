package com.akole.dividox.component.market.data.datasource

import com.akole.dividox.component.market.domain.model.StockQuote

interface StockQuoteLocalDataSource {
    suspend fun getQuotes(tickers: List<String>): Map<String, CachedQuote>
    suspend fun saveQuotes(quotes: Map<String, CachedQuote>)

    data class CachedQuote(val quote: StockQuote, val cachedAt: Long)
}
