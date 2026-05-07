package com.akole.dividox.integration.dividend.domain.model

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.market.domain.model.CompanyInfo
import com.akole.dividox.component.market.domain.model.DividendInfo

/**
 * A [DividendPayment] enriched with optional market metadata.
 *
 * [companyInfo] and [dividendInfo] may be `null` when the market data source is
 * unavailable. [dividendInfo] is only populated for upcoming payments — it carries
 * the real ex-dividend date and next payment date from the Yahoo Finance quote.
 */
data class EnrichedPayment(
    val payment: DividendPayment,
    val companyInfo: CompanyInfo?,
    val dividendInfo: DividendInfo? = null,
)
