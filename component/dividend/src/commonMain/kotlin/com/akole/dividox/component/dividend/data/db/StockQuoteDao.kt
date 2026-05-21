package com.akole.dividox.component.dividend.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface StockQuoteDao {
    @Query("SELECT * FROM stock_quote_cache WHERE ticker IN (:tickers)")
    suspend fun getByTickers(tickers: List<String>): List<StockQuoteEntity>

    @Upsert
    suspend fun upsertAll(quotes: List<StockQuoteEntity>)

    @Query("DELETE FROM stock_quote_cache WHERE cached_at < :expiryMs")
    suspend fun deleteExpired(expiryMs: Long)
}
