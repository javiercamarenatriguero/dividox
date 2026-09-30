package com.akole.dividox.market

import com.akole.dividox.component.dividend.data.db.DividendInfoDao
import com.akole.dividox.component.dividend.data.db.DividendInfoEntity
import com.akole.dividox.component.market.data.datasource.DividendInfoLocalDataSource
import com.akole.dividox.component.market.data.datasource.DividendInfoLocalDataSource.CachedDividendInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import kotlin.time.Clock
import kotlinx.datetime.LocalDate

class RoomDividendInfoLocalDataSource(
    private val dao: DividendInfoDao,
) : DividendInfoLocalDataSource {

    override suspend fun get(ticker: String): CachedDividendInfo? {
        return dao.getByTickers(listOf(ticker)).firstOrNull()?.let { entity ->
            CachedDividendInfo(info = entity.toDomain(), cachedAt = entity.cachedAt)
        }
    }

    override suspend fun save(ticker: String, info: DividendInfo, cachedAt: Long) {
        dao.upsertAll(listOf(info.toEntity(cachedAt)))
        val cutoff = Clock.System.now().toEpochMilliseconds() - PURGE_TTL_MS
        dao.deleteExpired(cutoff)
    }

    private fun DividendInfoEntity.toDomain() = DividendInfo(
        ticker = ticker,
        yield = yield,
        annualPayout = annualPayout,
        payoutRatio = payoutRatio,
        fiveYearGrowth = fiveYearGrowth,
        exDividendDate = exDividendDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        nextDividendDate = nextDividendDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    )

    private fun DividendInfo.toEntity(cachedAt: Long) = DividendInfoEntity(
        ticker = ticker,
        yield = yield,
        annualPayout = annualPayout,
        payoutRatio = payoutRatio,
        fiveYearGrowth = fiveYearGrowth,
        exDividendDate = exDividendDate?.toString(),
        nextDividendDate = nextDividendDate?.toString(),
        cachedAt = cachedAt,
    )

    companion object {
        private const val PURGE_TTL_MS = 604_800_000L // 7d
    }
}
