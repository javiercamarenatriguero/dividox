package com.akole.dividox.component.dividend.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DividendInfoDao {
    @Query("SELECT * FROM dividend_info_cache WHERE ticker IN (:tickers)")
    suspend fun getByTickers(tickers: List<String>): List<DividendInfoEntity>

    @Upsert
    suspend fun upsertAll(items: List<DividendInfoEntity>)

    @Query("DELETE FROM dividend_info_cache WHERE cached_at < :expiryMs")
    suspend fun deleteExpired(expiryMs: Long)
}
