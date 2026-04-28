package com.akole.dividox.component.market.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class SearchResponseDto(
    val quotes: List<SearchQuoteDto>? = null,
)

@Serializable
internal data class SearchQuoteDto(
    val symbol: String,
    val shortname: String? = null,
    val longname: String? = null,
    val exchDisp: String? = null,
    val quoteType: String? = null,
)
