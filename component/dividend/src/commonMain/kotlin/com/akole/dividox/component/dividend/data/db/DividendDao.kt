package com.akole.dividox.component.dividend.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for dividend payment persistence.
 *
 * All queries use the `payment_date` column (ISO-8601 YYYY-MM-DD) which allows
 * SQLite `strftime` date functions.
 */
@Dao
interface DividendDao {

    /**
     * Observes all dividend payments, newest first.
     * Emits whenever the table changes.
     */
    @Query("SELECT * FROM dividend_payments ORDER BY payment_date DESC")
    fun observeAll(): Flow<List<DividendPaymentEntity>>

    /**
     * Returns the sum of CASH payments for a given year.
     *
     * @param year Four-digit year string, e.g. "2025".
     * @return Sum of [DividendPaymentEntity.amount] for matching rows; 0.0 if none.
     */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM dividend_payments WHERE strftime('%Y', payment_date) = :year AND method = 'CASH'")
    suspend fun sumByYear(year: String): Double

    /**
     * Returns the sum of all CASH payments ever recorded.
     */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM dividend_payments WHERE method = 'CASH'")
    suspend fun sumLifetime(): Double

    /**
     * Observes payments with a future payment date (upcoming dividends).
     *
     * @param today Today's date in ISO-8601 format (YYYY-MM-DD).
     */
    @Query("SELECT * FROM dividend_payments WHERE payment_date > :today ORDER BY payment_date ASC")
    fun observeUpcoming(today: String): Flow<List<DividendPaymentEntity>>

    /**
     * Inserts or updates a list of payment entities.
     * Conflict strategy: replace on primary key collision.
     */
    @Upsert
    suspend fun upsert(payments: List<DividendPaymentEntity>)

    /** Deletes all cached payments (called on logout or cache invalidation). */
    @Query("DELETE FROM dividend_payments")
    suspend fun clearAll()
}
