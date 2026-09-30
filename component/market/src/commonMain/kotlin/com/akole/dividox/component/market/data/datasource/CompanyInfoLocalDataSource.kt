package com.akole.dividox.component.market.data.datasource

import com.akole.dividox.component.market.domain.model.CompanyInfo

interface CompanyInfoLocalDataSource {
    suspend fun get(ticker: String): CachedCompanyInfo?
    suspend fun save(ticker: String, info: CompanyInfo, cachedAt: Long)

    data class CachedCompanyInfo(val info: CompanyInfo, val cachedAt: Long)
}
