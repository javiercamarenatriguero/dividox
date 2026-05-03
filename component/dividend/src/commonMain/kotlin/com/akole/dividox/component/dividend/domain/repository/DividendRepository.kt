package com.akole.dividox.component.dividend.domain.repository

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import kotlinx.coroutines.flow.Flow

/**
 * Contract for accessing and persisting dividend payment data.
 *
 * Remote (Firestore) is the source of truth; local (Room) serves as the read cache.
 */
interface DividendRepository {

    /**
     * Observes all dividend payments, ordered by payment date descending.
     * Emits the cached list immediately, then re-emits on remote changes.
     */
    fun getDividendHistory(): Flow<List<DividendPayment>>

    /**
     * Observes the cumulative sum of all CASH dividend payments in the user's base currency.
     * REINVESTED payments are excluded.
     */
    fun getLifetimeDividends(): Flow<Double>

    /**
     * Observes the year-to-date sum of CASH dividend payments in the user's base currency.
     */
    fun getYtdDividends(): Flow<Double>

    /**
     * Observes dividends with a future payment date (upcoming scheduled payments).
     */
    fun getUpcomingPayments(): Flow<List<DividendPayment>>

    /**
     * Persists a new dividend payment to the remote source (Firestore).
     * The local cache is updated reactively via the Firestore snapshot listener.
     *
     * @param payment The payment to record.
     */
    suspend fun addDividendPayment(payment: DividendPayment)
}
