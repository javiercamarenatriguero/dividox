package com.akole.dividox.component.dividend.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_cache WHERE cache_key = :cacheKey ORDER BY position ASC")
    suspend fun getByCacheKey(cacheKey: String): List<NewsCacheEntity>

    @Upsert
    suspend fun upsertAll(items: List<NewsCacheEntity>)

    @Query("DELETE FROM news_cache WHERE cache_key = :cacheKey")
    suspend fun deleteByCacheKey(cacheKey: String)

    @Query("DELETE FROM news_cache WHERE cached_at < :expiryMs")
    suspend fun deleteExpired(expiryMs: Long)

    @Transaction
    suspend fun replaceForKey(cacheKey: String, items: List<NewsCacheEntity>) {
        deleteByCacheKey(cacheKey)
        upsertAll(items)
    }
}
