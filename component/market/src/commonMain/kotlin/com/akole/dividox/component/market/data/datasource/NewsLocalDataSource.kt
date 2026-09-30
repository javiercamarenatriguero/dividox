package com.akole.dividox.component.market.data.datasource

import com.akole.dividox.component.market.domain.model.NewsItem

interface NewsLocalDataSource {
    suspend fun get(cacheKey: String): CachedNews?
    suspend fun save(cacheKey: String, items: List<NewsItem>, cachedAt: Long)

    data class CachedNews(val items: List<NewsItem>, val cachedAt: Long)
}
