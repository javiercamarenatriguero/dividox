package com.akole.dividox.integration.dividend.domain.model

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.market.domain.model.CompanyInfo

/**
 * A [DividendPayment] enriched with optional [CompanyInfo] from the market component.
 *
 * [companyInfo] may be `null` when the market data source is unavailable or the
 * ticker is not found, so consumers must handle the absent case gracefully.
 *
 * @property payment The raw dividend payment record.
 * @property companyInfo Company metadata (name, logo) for the paying ticker.
 *   `null` when unavailable.
 */
data class EnrichedPayment(
    val payment: DividendPayment,
    val companyInfo: CompanyInfo?,
)
