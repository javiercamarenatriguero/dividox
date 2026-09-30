package com.akole.dividox.component.dividend.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dividend_info_cache")
data class DividendInfoEntity(
    @PrimaryKey val ticker: String,
    val yield: Double,
    @ColumnInfo(name = "annual_payout") val annualPayout: Double,
    @ColumnInfo(name = "payout_ratio") val payoutRatio: Double,
    @ColumnInfo(name = "five_year_growth") val fiveYearGrowth: Double,
    @ColumnInfo(name = "ex_dividend_date") val exDividendDate: String? = null,
    @ColumnInfo(name = "next_dividend_date") val nextDividendDate: String? = null,
    @ColumnInfo(name = "cached_at") val cachedAt: Long,
)
