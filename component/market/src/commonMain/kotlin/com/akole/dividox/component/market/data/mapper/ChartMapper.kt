package com.akole.dividox.component.market.data.mapper

import com.akole.dividox.component.market.data.dto.ChartMetaDto
import com.akole.dividox.component.market.data.dto.ChartResultDto
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.model.StockQuote
import kotlin.math.pow
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val SECONDS_PER_HOUR = 3_600L
private const val HOURS_PER_DAY = 24L
private const val DAYS_PER_YEAR = 365L
private const val DAYS_PER_YEAR_PRECISE = 365.25

/** Unix seconds in one calendar year (non-leap). */
private val ONE_YEAR_SECONDS = DAYS_PER_YEAR * HOURS_PER_DAY * SECONDS_PER_HOUR

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
        name = longName ?: shortName,
    )
}

/**
 * Derives [DividendInfo] from a chart result that includes `events.dividends`.
 *
 * Annual payout = sum of dividends paid in the last 12 months. This correctly handles
 * any payment cadence (quarterly, semi-annual, annual) without inflating the yield.
 * Falls back to the most recent single payment if no dividend was paid in the last year.
 *
 * Five-year CAGR is calculated from the first to last dividend in the event history;
 * the number of years is derived from actual Unix timestamps.
 *
 * @param ticker Yahoo Finance symbol used as the [DividendInfo.ticker] key.
 */
internal fun ChartResultDto.toDividendInfo(ticker: String): DividendInfo {
    val price = meta.regularMarketPrice
    val dividends = events?.dividends?.values?.sortedBy { it.date } ?: emptyList()

    val annualPayout = calculateAnnualPayout(dividends)
    val yieldValue = if (price > 0.0) (annualPayout / price) * 100.0 else 0.0
    val fiveYearGrowth = calculateFiveYearGrowth(dividends)
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
 * Sums dividends paid within the last 12 months.
 * Falls back to the last single payment when the trailing-year window is empty
 * (e.g. annual payer whose last event was just over a year ago).
 */
private fun calculateAnnualPayout(
    dividends: List<com.akole.dividox.component.market.data.dto.DividendEventDto>,
): Double {
    val oneYearAgoSeconds = Clock.System.now().epochSeconds - ONE_YEAR_SECONDS
    val trailingYear = dividends.filter { it.date >= oneYearAgoSeconds }.sumOf { it.amount }
    return if (trailingYear > 0.0) trailingYear else dividends.lastOrNull()?.amount ?: 0.0
}

/**
 * Computes annualised dividend CAGR between the oldest and newest events.
 * Returns 0.0 when fewer than 2 data points are available.
 */
private fun calculateFiveYearGrowth(
    dividends: List<com.akole.dividox.component.market.data.dto.DividendEventDto>,
): Double {
    if (dividends.size < 2) return 0.0
    val oldest = dividends.first().amount
    val newest = dividends.last().amount
    val years = (dividends.last().date - dividends.first().date).toDouble() /
        (DAYS_PER_YEAR_PRECISE * HOURS_PER_DAY * SECONDS_PER_HOUR)
    return if (oldest > 0.0 && years > 0.0) ((newest / oldest).pow(1.0 / years) - 1.0) * 100.0 else 0.0
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

