package com.akole.dividox.component.dividend.domain.repository

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Contract for accessing and persisting dividend payment data.
 *
 * Remote (Firestore) is the source of truth; local (Room) serves as the read cache.
 */
interface DividendRepository {

    /**
     * Observes only confirmed historical dividend payments (payment date ≤ today),
     * ordered by payment date descending.
     * Future-projected events from the market API are excluded — use [getUpcomingPayments] for those.
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
     * Observes the sum of dividend payments received since [from] (inclusive).
     */
    fun getDividendsSince(from: LocalDate): Flow<Double>

    /**
     * Observes dividends with a future payment date (upcoming scheduled payments).
     */
    fun getUpcomingPayments(): Flow<List<DividendPayment>>

    /**
     * Replaces the entire local dividend cache with the given list.
     * Called by the sync use case to ensure stale pre-purchase dividends are removed.
     *
     * @param payments The complete set of eligible payments to persist.
     */
    suspend fun replaceAllPayments(payments: List<DividendPayment>)

    /**
     * Deletes all locally cached dividend payments.
     * Must be called on sign-out to prevent stale data leaking into the next session.
     */
    suspend fun clearAll()

    /**
     * Persists or updates a single dividend payment in the local cache.
     *
     * @param payment The payment to record.
     */
    suspend fun addDividendPayment(payment: DividendPayment)
}
