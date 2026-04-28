package com.akole.dividox.component.market.domain.model

data class CompanyInfo(
    val ticker: String,
    val name: String,
    val exchange: String,
    val logoUrl: String?,
)
