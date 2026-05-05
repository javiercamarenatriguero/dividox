package com.akole.dividox.component.dividend.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a persisted dividend payment.
 *
 * @property id Unique payment identifier (Firestore document ID).
 * @property tickerId Ticker symbol of the holding.
 * @property amount Gross dividend amount (amountPerShare × shares).
 * @property amountPerShare Dividend declared per share for this event.
 * @property shares Number of shares held on the ex-dividend date.
 * @property currency ISO 4217 currency code.
 * @property paymentDate ISO-8601 date string (YYYY-MM-DD) used for SQL date functions.
 */
@Entity(tableName = "dividend_payments")
data class DividendPaymentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "ticker_id") val tickerId: String,
    val amount: Double,
    @ColumnInfo(name = "amount_per_share", defaultValue = "0.0") val amountPerShare: Double = 0.0,
    @ColumnInfo(defaultValue = "0.0") val shares: Double = 0.0,
    val currency: String,
    @ColumnInfo(name = "payment_date") val paymentDate: String,
)
