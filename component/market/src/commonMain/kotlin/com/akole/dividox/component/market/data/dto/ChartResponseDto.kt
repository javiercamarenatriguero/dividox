package com.akole.dividox.component.market.data.dto

import kotlinx.serialization.Serializable

/** Top-level wrapper returned by `v8/finance/chart`. */
@Serializable
internal data class ChartResponseDto(
    val chart: ChartResultWrapperDto,
)

/** Contains the list of chart results (usually one) or an error. */
@Serializable
internal data class ChartResultWrapperDto(
    val result: List<ChartResultDto>?,
    val error: ErrorDto?,
)

/**
 * A single chart result containing metadata, OHLCV timestamps/indicators,
 * and optional corporate events (dividends, splits).
 */
@Serializable
internal data class ChartResultDto(
    val meta: ChartMetaDto,
    val timestamp: List<Long>? = null,
    val indicators: IndicatorsDto? = null,
    /** Present only when `events=dividends` query parameter is included. */
    val events: ChartEventsDto? = null,
)

/**
 * Symbol-level metadata included in every chart response.
 *
 * [longName] and [shortName] are only populated when the `events` parameter is used.
 */
@Serializable
internal data class ChartMetaDto(
    val symbol: String,
    /** Full legal name (e.g. "Apple Inc."). Present in event-enriched responses. */
    val longName: String? = null,
    /** Short display name (e.g. "Apple Inc"). Fallback when [longName] is absent. */
    val shortName: String? = null,
    val regularMarketPrice: Double,
    val chartPreviousClose: Double? = null,
    val currency: String? = null,
    /** Unix timestamp of the last market price update. */
    val regularMarketTime: Long? = null,
    /** Exchange identifier (e.g. "NMS", "NYQ"). */
    val exchangeName: String? = null,
)

/** Corporate events embedded in the chart response when `events=dividends` is requested. */
@Serializable
internal data class ChartEventsDto(
    /** Map of Unix-timestamp-string → dividend event. Keys are Unix seconds as strings. */
    val dividends: Map<String, DividendEventDto>? = null,
)

/** A single cash dividend payment. */
@Serializable
internal data class DividendEventDto(
    /** Dividend amount per share in the security's native currency. */
    val amount: Double,
    /** Ex-dividend date as a Unix timestamp (seconds). */
    val date: Long,
)

/** Wrapper for price indicators (OHLCV arrays). */
@Serializable
internal data class IndicatorsDto(
    val quote: List<QuoteIndicatorDto>? = null,
)

/** Parallel arrays of OHLCV data aligned with [ChartResultDto.timestamp]. */
@Serializable
internal data class QuoteIndicatorDto(
    /** Closing prices; may contain `null` for missing candles. */
    val close: List<Double?>? = null,
)

/** API-level error descriptor. */
@Serializable
internal data class ErrorDto(
    val code: String,
    val description: String,
)
