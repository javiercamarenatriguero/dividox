package com.akole.dividox.component.dividend.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * One row per news item, keyed by the query cache key + item id.
 * Multiple news queries coexist (e.g. per default market) without collisions.
 */
@Entity(
    tableName = "news_cache",
    primaryKeys = ["cache_key", "id"],
)
data class NewsCacheEntity(
    @ColumnInfo(name = "cache_key") val cacheKey: String,
    val id: String,
    val position: Int,
    val title: String,
    val publisher: String,
    val link: String,
    @ColumnInfo(name = "published_at") val publishedAt: Long,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String? = null,
    val summary: String? = null,
    @ColumnInfo(name = "cached_at") val cachedAt: Long,
)
