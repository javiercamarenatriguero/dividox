package com.akole.dividox.market

import com.akole.dividox.component.dividend.data.db.StockQuoteDao
import com.akole.dividox.component.dividend.data.db.StockQuoteEntity
import com.akole.dividox.component.market.data.datasource.StockQuoteLocalDataSource
import com.akole.dividox.component.market.data.datasource.StockQuoteLocalDataSource.CachedQuote
import com.akole.dividox.component.market.domain.model.StockQuote
import kotlin.time.Clock
import kotlinx.datetime.Instant

class RoomStockQuoteLocalDataSource(
    private val dao: StockQuoteDao,
) : StockQuoteLocalDataSource {

    override suspend fun getQuotes(tickers: List<String>): Map<String, CachedQuote> {
        return dao.getByTickers(tickers).associate { entity ->
            entity.ticker to CachedQuote(
                quote = entity.toStockQuote(),
                cachedAt = entity.cachedAt,
            )
        }
    }

    override suspend fun saveQuotes(quotes: Map<String, CachedQuote>) {
        val entities = quotes.map { (ticker, cached) ->
            cached.quote.toEntity(ticker, cached.cachedAt)
        }
        dao.upsertAll(entities)
        // Purge entries older than 24h to keep the DB tidy
        val cutoff = Clock.System.now().toEpochMilliseconds() - PURGE_TTL_MS
        dao.deleteExpired(cutoff)
    }

    private fun StockQuoteEntity.toStockQuote() = StockQuote(
        ticker = ticker,
        price = price,
        change = change,
        changePercent = changePercent,
        currency = currency,
        lastUpdated = Instant.fromEpochMilliseconds(lastUpdated),
        name = name,
        exchange = exchange,
        fiftyTwoWeekHigh = fiftyTwoWeekHigh,
        fiftyTwoWeekLow = fiftyTwoWeekLow,
        volume = volume,
        dayHigh = dayHigh,
        dayLow = dayLow,
    )

    private fun StockQuote.toEntity(ticker: String, cachedAt: Long) = StockQuoteEntity(
        ticker = ticker,
        price = price,
        change = change,
        changePercent = changePercent,
        currency = currency,
        lastUpdated = lastUpdated.toEpochMilliseconds(),
        cachedAt = cachedAt,
        name = name,
        exchange = exchange,
        fiftyTwoWeekHigh = fiftyTwoWeekHigh,
        fiftyTwoWeekLow = fiftyTwoWeekLow,
        volume = volume,
        dayHigh = dayHigh,
        dayLow = dayLow,
    )

    companion object {
        private const val PURGE_TTL_MS = 86_400_000L // 24h
    }
}
