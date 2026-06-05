package com.akole.dividox.component.market.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class SearchResponseDto(
    val quotes: List<SearchQuoteDto>? = null,
    val news: List<NewsItemDto>? = null,
)

@Serializable
internal data class NewsItemDto(
    val uuid: String,
    val title: String,
    val publisher: String? = null,
    val link: String,
    val providerPublishTime: Long,
    val thumbnail: NewsThumbDto? = null,
)

@Serializable
internal data class NewsThumbDto(
    val resolutions: List<NewsThumbResolutionDto>? = null,
)

@Serializable
internal data class NewsThumbResolutionDto(
    val url: String,
    val width: Int = 0,
    val height: Int = 0,
    val tag: String? = null,
)

@Serializable
internal data class SearchQuoteDto(
    val symbol: String,
    val shortname: String? = null,
    val longname: String? = null,
    val exchDisp: String? = null,
    val quoteType: String? = null,
)
