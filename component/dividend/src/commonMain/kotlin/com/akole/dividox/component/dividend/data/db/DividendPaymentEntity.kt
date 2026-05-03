package com.akole.dividox.component.dividend.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a persisted dividend payment.
 *
 * @property id Unique payment identifier (Firestore document ID).
 * @property tickerId Ticker symbol of the holding.
 * @property amount Gross dividend amount.
 * @property currency ISO 4217 currency code.
 * @property paymentDate ISO-8601 date string (YYYY-MM-DD) used for SQL date functions.
 */
@Entity(tableName = "dividend_payments")
data class DividendPaymentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "ticker_id") val tickerId: String,
    val amount: Double,
    val currency: String,
    @ColumnInfo(name = "payment_date") val paymentDate: String,
)
