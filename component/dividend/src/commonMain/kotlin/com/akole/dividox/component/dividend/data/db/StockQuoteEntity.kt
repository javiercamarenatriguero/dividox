package com.akole.dividox.component.dividend.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_quote_cache")
data class StockQuoteEntity(
    @PrimaryKey val ticker: String,
    val price: Double,
    val change: Double,
    @ColumnInfo(name = "change_percent") val changePercent: Double,
    val currency: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long,
    @ColumnInfo(name = "cached_at") val cachedAt: Long,
    val name: String? = null,
    val exchange: String? = null,
    @ColumnInfo(name = "fifty_two_week_high") val fiftyTwoWeekHigh: Double? = null,
    @ColumnInfo(name = "fifty_two_week_low") val fiftyTwoWeekLow: Double? = null,
    val volume: Long? = null,
    @ColumnInfo(name = "day_high") val dayHigh: Double? = null,
    @ColumnInfo(name = "day_low") val dayLow: Double? = null,
)
