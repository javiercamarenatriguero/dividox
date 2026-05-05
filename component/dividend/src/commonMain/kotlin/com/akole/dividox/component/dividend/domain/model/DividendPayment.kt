package com.akole.dividox.component.dividend.domain.model

import kotlinx.datetime.LocalDate

/**
 * Represents a single dividend payment received for a holding.
 *
 * @property id Unique identifier for this payment.
 * @property tickerId The ticker symbol of the holding (e.g., "AAPL").
 * @property amount The gross dividend amount received (amountPerShare × shares).
 * @property amountPerShare The dividend declared per share for this event.
 * @property shares The number of shares held on the ex-dividend date.
 * @property currency ISO 4217 currency code (e.g., "USD").
 * @property paymentDate The date the dividend was paid or credited.
 */
data class DividendPayment(
    val id: DividendPaymentId,
    val tickerId: String,
    val amount: Double,
    val amountPerShare: Double,
    val shares: Double,
    val currency: String,
    val paymentDate: LocalDate,
)
