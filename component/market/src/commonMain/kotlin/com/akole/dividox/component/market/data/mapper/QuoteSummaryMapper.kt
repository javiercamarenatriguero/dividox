package com.akole.dividox.component.market.data.mapper

import com.akole.dividox.component.market.data.dto.QuoteSummaryResultDto
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun QuoteSummaryResultDto.toDividendInfo(ticker: String): DividendInfo {
    val summary = summaryDetail
    val exDivDate = summary?.exDividendDate?.raw?.toLong()?.let { epochSecs ->
        Instant.fromEpochSeconds(epochSecs)
            .toLocalDateTime(TimeZone.UTC)
            .date
    }
    return DividendInfo(
        ticker = ticker,
        yield = summary?.dividendYield?.raw ?: 0.0,
        annualPayout = summary?.dividendRate?.raw ?: 0.0,
        payoutRatio = summary?.payoutRatio?.raw ?: 0.0,
        fiveYearGrowth = summary?.fiveYearAvgDividendYield?.raw ?: 0.0,
        exDividendDate = exDivDate,
    )
}

internal fun QuoteSummaryResultDto.toCompanyInfo(ticker: String): CompanyInfo {
    val profile = assetProfile
    return CompanyInfo(
        ticker = ticker,
        name = profile?.longName ?: profile?.shortName ?: ticker,
        exchange = profile?.exchange ?: "",
        logoUrl = null,
    )
}
