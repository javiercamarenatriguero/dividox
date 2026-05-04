package com.akole.dividox.component.dividend.data.repository

import com.akole.dividox.component.dividend.data.datasource.DividendLocalDataSource
import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Repository implementation for dividend payments.
 *
 * **Architecture — Room as single source of truth:**
 * Dividend data is fully derived from the user's holdings combined with the market API
 * (see `SyncDividendHistoryFromHoldingsUseCase`). There is no Firestore persistence —
 * dividends are always recomputable and are written directly to Room.
 *
 * @property local Room-backed local data source.
 */
class DividendRepositoryImpl(
    private val local: DividendLocalDataSource,
) : DividendRepository {

    override fun getDividendHistory(): Flow<List<DividendPayment>> = local.observeAll()

    override fun getLifetimeDividends(): Flow<Double> =
        local.observeAll().map { payments -> payments.sumOf { it.amount } }

    override fun getYtdDividends(): Flow<Double> {
        val currentYear = Clock.System.todayIn(TimeZone.UTC).year
        return local.observeAll().map { payments ->
            payments.filter { it.paymentDate.year == currentYear }.sumOf { it.amount }
        }
    }

    override fun getDividendsSince(from: LocalDate): Flow<Double> =
        local.observeAll().map { payments ->
            payments.filter { it.paymentDate >= from }.sumOf { it.amount }
        }

    override fun getUpcomingPayments(): Flow<List<DividendPayment>> {
        val today = Clock.System.todayIn(TimeZone.UTC).toString()
        return local.observeUpcoming(today)
    }

    override suspend fun addDividendPayment(payment: DividendPayment) {
        local.upsert(payment)
    }

    override suspend fun replaceAllPayments(payments: List<DividendPayment>) {
        local.replaceAll(payments)
    }

    override suspend fun clearAll() {
        local.clearAll()
    }
}
