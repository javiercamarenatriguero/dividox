package com.akole.dividox.component.dividend.domain.model

import kotlinx.datetime.LocalDate

/**
 * Represents a single dividend payment received for a holding.
 *
 * @property id Unique identifier for this payment.
 * @property tickerId The ticker symbol of the holding (e.g., "AAPL").
 * @property amount The gross dividend amount received.
 * @property currency ISO 4217 currency code (e.g., "USD").
 * @property paymentDate The date the dividend was paid or credited.
 * @property method Whether the dividend was received as cash or reinvested.
 */
data class DividendPayment(
    val id: DividendPaymentId,
    val tickerId: String,
    val amount: Double,
    val currency: String,
    val paymentDate: LocalDate,
    val method: PaymentMethod,
)
