package com.akole.dividox.component.market.data.mapper

import com.akole.dividox.component.market.data.dto.ChartMetaDto
import com.akole.dividox.component.market.data.dto.ChartResultDto
import com.akole.dividox.component.market.data.dto.QuoteResultDto
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import kotlin.time.Instant

internal fun ChartMetaDto.toStockQuote(): StockQuote {
    val previousClose = chartPreviousClose ?: regularMarketPrice
    val change = regularMarketPrice - previousClose
    val changePercent = if (previousClose != 0.0) (change / previousClose) * 100.0 else 0.0
    return StockQuote(
        ticker = symbol,
        price = regularMarketPrice,
        change = change,
        changePercent = changePercent,
        currency = currency ?: "USD",
        lastUpdated = Instant.fromEpochSeconds(regularMarketTime ?: 0),
    )
}

internal fun QuoteResultDto.toStockQuote(): StockQuote = StockQuote(
    ticker = symbol,
    price = regularMarketPrice ?: 0.0,
    change = regularMarketChange ?: 0.0,
    changePercent = regularMarketChangePercent ?: 0.0,
    currency = currency ?: "USD",
    lastUpdated = Instant.fromEpochSeconds(regularMarketTime ?: 0),
)

internal fun ChartResultDto.toPricePoints(): List<PricePoint> {
    val timestamps = timestamp ?: return emptyList()
    val closes = indicators?.quote?.firstOrNull()?.close ?: return emptyList()
    return timestamps.zip(closes)
        .mapNotNull { (ts, close) ->
            close?.let { PricePoint(timestamp = Instant.fromEpochSeconds(ts), close = it) }
        }
}
