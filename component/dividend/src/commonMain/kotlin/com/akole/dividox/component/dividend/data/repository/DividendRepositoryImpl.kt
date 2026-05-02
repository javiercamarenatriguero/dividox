package com.akole.dividox.component.dividend.data.repository

import com.akole.dividox.component.dividend.data.datasource.DividendLocalDataSource
import com.akole.dividox.component.dividend.data.datasource.DividendRemoteDataSource
import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/**
 * Repository implementation for dividend payments.
 *
 * Architecture:
 * - **Firestore** is the source of truth. A snapshot listener syncs changes into Room automatically.
 * - **Room** is the read cache — the UI always observes Room.
 * - Write operations go directly to Firestore; Room is updated via the snapshot listener.
 *
 * Cache sync strategy:
 * 1. On construction, a Firestore snapshot listener is started in [syncScope].
 * 2. Each Firestore emission calls [DividendLocalDataSource.replaceAll] to refresh the cache.
 *
 * @property local Room-backed local data source.
 * @property remote Firestore-backed remote data source.
 */
class DividendRepositoryImpl(
    private val local: DividendLocalDataSource,
    private val remote: DividendRemoteDataSource,
) : DividendRepository {

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        syncScope.launch {
            remote.observeAll().collect { payments ->
                local.replaceAll(payments)
            }
        }
    }

    override fun getDividendHistory(): Flow<List<DividendPayment>> = local.observeAll()

    override fun getLifetimeDividends(): Flow<Double> = flow {
        emit(local.sumLifetime())
    }

    override fun getYtdDividends(): Flow<Double> = flow {
        val currentYear = Clock.System.todayIn(TimeZone.UTC).year.toString()
        emit(local.sumByYear(currentYear))
    }

    override fun getUpcomingPayments(): Flow<List<DividendPayment>> {
        val today = Clock.System.todayIn(TimeZone.UTC).toString()
        return local.observeUpcoming(today)
    }

    override suspend fun addDividendPayment(payment: DividendPayment) {
        remote.add(payment)
    }
}
