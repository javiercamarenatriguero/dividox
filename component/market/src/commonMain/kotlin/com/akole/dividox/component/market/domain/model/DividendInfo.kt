package com.akole.dividox.component.market.domain.model

import kotlinx.datetime.LocalDate

data class DividendInfo(
    val ticker: String,
    val yield: Double,
    val annualPayout: Double,
    val payoutRatio: Double,
    val fiveYearGrowth: Double,
    val exDividendDate: LocalDate?,
)
