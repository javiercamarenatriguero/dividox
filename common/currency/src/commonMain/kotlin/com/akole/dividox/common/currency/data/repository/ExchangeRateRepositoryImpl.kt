package com.akole.dividox.common.currency.data.repository

import com.akole.dividox.common.currency.data.datasource.ExchangeRateDataSource
import com.akole.dividox.common.currency.data.datasource.LocalExchangeRateDataSource
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates
import com.akole.dividox.common.currency.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * [ExchangeRateRepository] implementation with a 3-tier cache strategy:
 *
 * - **L1 — In-memory** (`cache` map, guarded by [Mutex]): fastest; lives for the app process lifetime.
 * - **L2 — DataStore** ([localDataSource]): survives app restarts; avoids unnecessary API calls.
 * - **L3 — Network** ([remoteDataSource]): called only when neither L1 nor L2 has today's rates.
 *
 * Rates are considered fresh when [ExchangeRates.date] equals today's date from [todayProvider].
 * On a successful network fetch, results are written to both L2 (DataStore) and L1 (memory).
 *
 * @param remoteDataSource Network data source (Frankfurter API).
 * @param localDataSource  Persistent data source (DataStore Preferences).
 * @param ioDispatcher     Coroutine dispatcher for I/O-bound work.
 * @param todayProvider    Returns today's [LocalDate]; injectable for deterministic testing.
 */
class ExchangeRateRepositoryImpl(
    private val remoteDataSource: ExchangeRateDataSource,
    private val localDataSource: LocalExchangeRateDataSource,
    private val ioDispatcher: CoroutineDispatcher,
    private val todayProvider: () -> LocalDate,
) : ExchangeRateRepository {

    private val cache = mutableMapOf<Currency, ExchangeRates>()
    private val mutex = Mutex()

    /**
     * Returns today's exchange rates for [base], resolving through the 3-tier cache.
     *
     * Resolution order:
     * 1. **L1** — return immediately if in-memory entry exists and its date equals today.
     * 2. **L2** — read from DataStore; if fresh, populate L1 and return.
     * 3. **L3** — fetch from the network; on success, persist to L2 and populate L1.
     *
     * @param base The reference currency for the rate table.
     * @return [Result.success] with [ExchangeRates] from the first valid tier,
     *         or [Result.failure] if the network call fails and no cached data exists.
     */
    override suspend fun getExchangeRates(base: Currency): Result<ExchangeRates> =
        withContext(ioDispatcher) {
            val today = todayProvider()

            // L1: in-memory cache
            mutex.withLock {
                val cached = cache[base]
                if (cached != null && cached.date == today) return@withContext Result.success(cached)
            }

            // L2: persistent DataStore cache
            val local = localDataSource.get(base)
            if (local != null && local.date == today) {
                mutex.withLock { cache[base] = local }
                return@withContext Result.success(local)
            }

            // L3: network
            remoteDataSource.getExchangeRates(base).also { result ->
                result.onSuccess { rates ->
                    localDataSource.save(rates)
                    mutex.withLock { cache[base] = rates }
                }
            }
        }
}
