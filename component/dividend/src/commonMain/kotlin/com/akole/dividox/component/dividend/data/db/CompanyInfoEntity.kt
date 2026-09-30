package com.akole.dividox.component.dividend.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_info_cache")
data class CompanyInfoEntity(
    @PrimaryKey val ticker: String,
    val name: String,
    val exchange: String,
    @ColumnInfo(name = "logo_url") val logoUrl: String? = null,
    @ColumnInfo(name = "cached_at") val cachedAt: Long,
)
