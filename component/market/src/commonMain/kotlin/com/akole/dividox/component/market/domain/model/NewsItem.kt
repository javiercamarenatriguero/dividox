package com.akole.dividox.component.market.domain.model

import kotlinx.datetime.Instant

data class NewsItem(
    val id: String,
    val title: String,
    val publisher: String,
    val link: String,
    val publishedAt: Instant,
    val thumbnailUrl: String?,
)
