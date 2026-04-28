package com.akole.dividox.component.market.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ChartResponseDto(
    val chart: ChartResultWrapperDto,
)

@Serializable
internal data class ChartResultWrapperDto(
    val result: List<ChartResultDto>?,
    val error: ErrorDto?,
)

@Serializable
internal data class ChartResultDto(
    val meta: ChartMetaDto,
    val timestamp: List<Long>? = null,
    val indicators: IndicatorsDto? = null,
)

@Serializable
internal data class ChartMetaDto(
    val symbol: String,
    val regularMarketPrice: Double,
    val chartPreviousClose: Double? = null,
    val currency: String? = null,
    val regularMarketTime: Long? = null,
    val exchangeName: String? = null,
)

@Serializable
internal data class IndicatorsDto(
    val quote: List<QuoteIndicatorDto>? = null,
)

@Serializable
internal data class QuoteIndicatorDto(
    val close: List<Double?>? = null,
)

@Serializable
internal data class ErrorDto(
    val code: String,
    val description: String,
)
