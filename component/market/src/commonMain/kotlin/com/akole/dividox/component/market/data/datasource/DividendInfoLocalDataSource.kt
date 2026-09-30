package com.akole.dividox.component.market.data.datasource

import com.akole.dividox.component.market.domain.model.DividendInfo

interface DividendInfoLocalDataSource {
    suspend fun get(ticker: String): CachedDividendInfo?
    suspend fun save(ticker: String, info: DividendInfo, cachedAt: Long)

    data class CachedDividendInfo(val info: DividendInfo, val cachedAt: Long)
}
