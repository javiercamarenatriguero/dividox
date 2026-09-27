package com.akole.dividox.market

import com.akole.dividox.component.dividend.data.db.NewsCacheEntity
import com.akole.dividox.component.dividend.data.db.NewsDao
import com.akole.dividox.component.market.data.datasource.NewsLocalDataSource
import com.akole.dividox.component.market.data.datasource.NewsLocalDataSource.CachedNews
import com.akole.dividox.component.market.domain.model.NewsItem
import kotlin.time.Clock
import kotlin.time.Instant

class RoomNewsLocalDataSource(
    private val dao: NewsDao,
) : NewsLocalDataSource {

    override suspend fun get(cacheKey: String): CachedNews? {
        val rows = dao.getByCacheKey(cacheKey)
        if (rows.isEmpty()) return null
        return CachedNews(
            items = rows.map { it.toDomain() },
            cachedAt = rows.first().cachedAt,
        )
    }

    override suspend fun save(cacheKey: String, items: List<NewsItem>, cachedAt: Long) {
        val entities = items.mapIndexed { index, item -> item.toEntity(cacheKey, index, cachedAt) }
        dao.replaceForKey(cacheKey, entities)
        // Purge entries older than the retention window to keep the DB tidy.
        val cutoff = Clock.System.now().toEpochMilliseconds() - PURGE_TTL_MS
        dao.deleteExpired(cutoff)
    }

    private fun NewsCacheEntity.toDomain() = NewsItem(
        id = id,
        title = title,
        publisher = publisher,
        link = link,
        publishedAt = Instant.fromEpochMilliseconds(publishedAt),
        thumbnailUrl = thumbnailUrl,
        summary = summary,
    )

    private fun NewsItem.toEntity(cacheKey: String, position: Int, cachedAt: Long) = NewsCacheEntity(
        cacheKey = cacheKey,
        id = id,
        position = position,
        title = title,
        publisher = publisher,
        link = link,
        publishedAt = publishedAt.toEpochMilliseconds(),
        thumbnailUrl = thumbnailUrl,
        summary = summary,
        cachedAt = cachedAt,
    )

    companion object {
        private const val PURGE_TTL_MS = 86_400_000L // 24h
    }
}
