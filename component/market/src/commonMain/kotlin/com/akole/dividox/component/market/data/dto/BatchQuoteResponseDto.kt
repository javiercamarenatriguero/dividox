package com.akole.dividox.component.market.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class BatchQuoteResponseDto(
    val quoteResponse: QuoteResponseDto,
)

@Serializable
internal data class QuoteResponseDto(
    val result: List<QuoteResultDto>?,
    val error: ErrorDto? = null,
)

@Serializable
internal data class QuoteResultDto(
    val symbol: String,
    val regularMarketPrice: Double? = null,
    val regularMarketChange: Double? = null,
    val regularMarketChangePercent: Double? = null,
    val currency: String? = null,
    val regularMarketTime: Long? = null,
)
