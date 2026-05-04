package com.akole.dividox.component.dividend.data.datasource

import com.akole.dividox.component.dividend.data.db.DividendDao
import com.akole.dividox.component.dividend.data.mapper.toDomain
import com.akole.dividox.component.dividend.data.mapper.toEntity
import com.akole.dividox.component.dividend.domain.model.DividendPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local data source backed by Room.
 *
 * Responsibilities:
 * - Expose [DividendDao] queries as domain-model streams.
 * - Translate between [DividendPaymentEntity] and [DividendPayment] via the mapper.
 *
 * @property dao Room DAO for raw database access.
 */
class DividendLocalDataSource(private val dao: DividendDao) {

    /** Observes all cached payments, newest first, as domain models. */
    fun observeAll(): Flow<List<DividendPayment>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    /** Returns the lifetime sum of CASH payments. */
    suspend fun sumLifetime(): Double = dao.sumLifetime()

    /**
     * Returns the year-to-date sum of CASH payments.
     *
     * @param year Four-digit year string, e.g. "2025".
     */
    suspend fun sumByYear(year: String): Double = dao.sumByYear(year)

    /**
     * Observes upcoming payments (future payment dates), as domain models.
     *
     * @param today Today's ISO-8601 date string.
     */
    fun observeUpcoming(today: String): Flow<List<DividendPayment>> =
        dao.observeUpcoming(today).map { entities -> entities.map { it.toDomain() } }

    /**
     * Atomically replaces the local cache with the given list of domain payments.
     * Delegates to [DividendDao.replaceAll] so Room emits a single notification.
     */
    suspend fun replaceAll(payments: List<DividendPayment>) {
        dao.replaceAll(payments.map { it.toEntity() })
    }

    /** Deletes all cached payments. Call on sign-out to prevent data leaking across sessions. */
    suspend fun clearAll() {
        dao.clearAll()
    }

    /** Appends or updates a single payment in the cache. */
    suspend fun upsert(payment: DividendPayment) {
        dao.upsert(listOf(payment.toEntity()))
    }
}
