package com.akole.dividox.component.market.data.mapper

import com.akole.dividox.component.market.data.dto.NewsItemDto
import com.akole.dividox.component.market.domain.model.NewsItem
import kotlinx.datetime.Instant

internal fun NewsItemDto.toNewsItem(): NewsItem = NewsItem(
    id = uuid,
    title = title,
    publisher = publisher ?: "",
    link = link,
    publishedAt = Instant.fromEpochSeconds(providerPublishTime),
    thumbnailUrl = thumbnail?.resolutions
        ?.firstOrNull { it.tag == "140x140" }
        ?.url
        ?: thumbnail?.resolutions?.firstOrNull()?.url,
)
