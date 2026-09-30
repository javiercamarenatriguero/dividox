package com.akole.dividox.market

import com.akole.dividox.component.dividend.data.db.CompanyInfoDao
import com.akole.dividox.component.dividend.data.db.CompanyInfoEntity
import com.akole.dividox.component.market.data.datasource.CompanyInfoLocalDataSource
import com.akole.dividox.component.market.data.datasource.CompanyInfoLocalDataSource.CachedCompanyInfo
import com.akole.dividox.component.market.domain.model.CompanyInfo
import kotlin.time.Clock

class RoomCompanyInfoLocalDataSource(
    private val dao: CompanyInfoDao,
) : CompanyInfoLocalDataSource {

    override suspend fun get(ticker: String): CachedCompanyInfo? {
        return dao.getByTickers(listOf(ticker)).firstOrNull()?.let { entity ->
            CachedCompanyInfo(
                info = entity.toDomain(),
                cachedAt = entity.cachedAt,
            )
        }
    }

    override suspend fun save(ticker: String, info: CompanyInfo, cachedAt: Long) {
        dao.upsertAll(listOf(info.toEntity(cachedAt)))
        // Purge entries older than the retention window to keep the DB tidy.
        val cutoff = Clock.System.now().toEpochMilliseconds() - PURGE_TTL_MS
        dao.deleteExpired(cutoff)
    }

    private fun CompanyInfoEntity.toDomain() = CompanyInfo(
        ticker = ticker,
        name = name,
        exchange = exchange,
        logoUrl = logoUrl,
    )

    private fun CompanyInfo.toEntity(cachedAt: Long) = CompanyInfoEntity(
        ticker = ticker,
        name = name,
        exchange = exchange,
        logoUrl = logoUrl,
        cachedAt = cachedAt,
    )

    companion object {
        private const val PURGE_TTL_MS = 604_800_000L // 7d
    }
}
