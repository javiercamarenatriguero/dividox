package com.akole.dividox.component.market.data.mapper

import com.akole.dividox.component.market.data.dto.ChartMetaDto
import com.akole.dividox.component.market.data.dto.ChartResultDto
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import kotlin.math.pow
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Maps chart metadata to a [StockQuote].
 *
 * Change and changePercent are derived from [ChartMetaDto.chartPreviousClose].
 * If previous close is unavailable, change values default to 0.
 */
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

/**
 * Derives [DividendInfo] from a chart result that includes `events.dividends`.
 *
 * Annual payout = sum of the last 4 dividend events (assumes quarterly cadence).
 * Five-year CAGR is calculated from the first to last dividend amount in the event history;
 * the number of years is derived from the actual timestamps.
 *
 * @param ticker Yahoo Finance symbol used as the [DividendInfo.ticker] key.
 */
internal fun ChartResultDto.toDividendInfo(ticker: String): DividendInfo {
    val price = meta.regularMarketPrice
    val dividends = events?.dividends?.values?.sortedBy { it.date } ?: emptyList()

    val recent = dividends.takeLast(4)
    val annualPayout = recent.sumOf { it.amount }

    val yieldValue = if (price > 0.0) (annualPayout / price) * 100.0 else 0.0

    // CAGR from first to last dividend; years derived from actual Unix timestamps
    val fiveYearGrowth = if (dividends.size >= 2) {
        val oldest = dividends.first().amount
        val newest = dividends.last().amount
        val years = (dividends.last().date - dividends.first().date).toDouble() / (365.25 * 24 * 3600)
        if (oldest > 0.0 && years > 0.0) ((newest / oldest).pow(1.0 / years) - 1.0) * 100.0 else 0.0
    } else 0.0

    val exDividendDate = dividends.lastOrNull()?.date?.let {
        Instant.fromEpochSeconds(it).toLocalDateTime(TimeZone.UTC).date
    }

    return DividendInfo(
        ticker = ticker,
        yield = yieldValue,
        annualPayout = annualPayout,
        payoutRatio = 0.0,
        fiveYearGrowth = fiveYearGrowth,
        exDividendDate = exDividendDate,
    )
}

/**
 * Maps chart result metadata to [CompanyInfo].
 *
 * [CompanyInfo.logoUrl] is always `null` — Yahoo Finance does not expose logos via this endpoint.
 *
 * @param ticker Yahoo Finance symbol used as the [CompanyInfo.ticker] key.
 */
internal fun ChartResultDto.toCompanyInfo(ticker: String): CompanyInfo = CompanyInfo(
    ticker = ticker,
    name = meta.longName ?: meta.shortName ?: ticker,
    exchange = meta.exchangeName ?: "",
    logoUrl = null,
)

/**
 * Maps parallel timestamp and close-price arrays to a list of [PricePoint].
 *
 * Candles with a `null` closing price (market holidays, partial trading days) are skipped.
 */
internal fun ChartResultDto.toPricePoints(): List<PricePoint> {
    val timestamps = timestamp ?: return emptyList()
    val closes = indicators?.quote?.firstOrNull()?.close ?: return emptyList()
    return timestamps.zip(closes)
        .mapNotNull { (ts, close) ->
            close?.let { PricePoint(timestamp = Instant.fromEpochSeconds(ts), close = it) }
        }
}
