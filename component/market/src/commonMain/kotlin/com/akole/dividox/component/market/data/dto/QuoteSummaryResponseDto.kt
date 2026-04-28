package com.akole.dividox.component.market.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class QuoteSummaryResponseDto(
    val quoteSummary: QuoteSummaryDto,
)

@Serializable
internal data class QuoteSummaryDto(
    val result: List<QuoteSummaryResultDto>?,
    val error: ErrorDto? = null,
)

@Serializable
internal data class QuoteSummaryResultDto(
    val summaryDetail: SummaryDetailDto? = null,
    val assetProfile: AssetProfileDto? = null,
)

@Serializable
internal data class SummaryDetailDto(
    val dividendYield: RawValueDto? = null,
    val dividendRate: RawValueDto? = null,
    val payoutRatio: RawValueDto? = null,
    @SerialName("fiveYearAvgDividendYield") val fiveYearAvgDividendYield: RawValueDto? = null,
    val exDividendDate: RawValueDto? = null,
)

@Serializable
internal data class AssetProfileDto(
    val longName: String? = null,
    val shortName: String? = null,
    val exchange: String? = null,
    val website: String? = null,
)

@Serializable
internal data class RawValueDto(
    val raw: Double? = null,
    val fmt: String? = null,
)
