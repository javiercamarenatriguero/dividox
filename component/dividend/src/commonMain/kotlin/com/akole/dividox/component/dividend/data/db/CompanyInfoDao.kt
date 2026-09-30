package com.akole.dividox.component.dividend.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CompanyInfoDao {
    @Query("SELECT * FROM company_info_cache WHERE ticker IN (:tickers)")
    suspend fun getByTickers(tickers: List<String>): List<CompanyInfoEntity>

    @Upsert
    suspend fun upsertAll(items: List<CompanyInfoEntity>)

    @Query("DELETE FROM company_info_cache WHERE cached_at < :expiryMs")
    suspend fun deleteExpired(expiryMs: Long)
}
