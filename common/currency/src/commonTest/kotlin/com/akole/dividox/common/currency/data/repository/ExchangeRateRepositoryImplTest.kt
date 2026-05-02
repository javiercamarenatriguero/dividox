package com.akole.dividox.common.currency.data.repository

import com.akole.dividox.common.currency.data.datasource.ExchangeRateDataSource
import com.akole.dividox.common.currency.data.datasource.LocalExchangeRateDataSource
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.common.currency.domain.model.ExchangeRates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExchangeRateRepositoryImplTest {

    private val today = LocalDate(2025, 1, 15)
    private val yesterday = LocalDate(2025, 1, 14)

    private val ratesForToday = ExchangeRates(
        base = Currency.EUR,
        date = today,
        rates = mapOf(Currency.USD to 1.05),
    )
    private val ratesForYesterday = ExchangeRates(
        base = Currency.EUR,
        date = yesterday,
        rates = mapOf(Currency.USD to 1.03),
    )

    private fun emptyLocalDataSource() = object : LocalExchangeRateDataSource {
        override suspend fun get(base: Currency): ExchangeRates? = null
        override suspend fun save(rates: ExchangeRates) = Unit
    }

    private fun buildRepo(
        remote: ExchangeRateDataSource,
        local: LocalExchangeRateDataSource = emptyLocalDataSource(),
        today: LocalDate = this.today,
    ) = ExchangeRateRepositoryImpl(
        remoteDataSource = remote,
        localDataSource = local,
        ioDispatcher = Dispatchers.Default,
        todayProvider = { today },
    )

    @Test
    fun `GIVEN no cache WHEN getExchangeRates THEN fetches from remote datasource`() = runTest {
        // GIVEN
        var callCount = 0
        val remote = object : ExchangeRateDataSource {
            override suspend fun getExchangeRates(base: Currency): Result<ExchangeRates> {
                callCount++
                return Result.success(ratesForToday)
            }
        }
        // WHEN
        val result = buildRepo(remote).getExchangeRates(Currency.EUR)
        // THEN
        assertEquals(1, callCount)
        assertEquals(ratesForToday, result.getOrThrow())
    }

    @Test
    fun `GIVEN in-memory cache hit WHEN getExchangeRates twice THEN remote called only once`() = runTest {
        // GIVEN
        var callCount = 0
        val remote = object : ExchangeRateDataSource {
            override suspend fun getExchangeRates(base: Currency): Result<ExchangeRates> {
                callCount++
                return Result.success(ratesForToday)
            }
        }
        val repo = buildRepo(remote)
        // WHEN
        repo.getExchangeRates(Currency.EUR) // populates in-memory
        repo.getExchangeRates(Currency.EUR) // should hit L1 cache
        // THEN
        assertEquals(1, callCount)
    }

    @Test
    fun `GIVEN local cache with today rates WHEN getExchangeRates THEN returns local without calling remote`() = runTest {
        // GIVEN
        var remoteCallCount = 0
        val remote = object : ExchangeRateDataSource {
            override suspend fun getExchangeRates(base: Currency): Result<ExchangeRates> {
                remoteCallCount++
                return Result.success(ratesForToday)
            }
        }
        val local = object : LocalExchangeRateDataSource {
            override suspend fun get(base: Currency) = ratesForToday
            override suspend fun save(rates: ExchangeRates) = Unit
        }
        // WHEN
        val result = buildRepo(remote, local).getExchangeRates(Currency.EUR)
        // THEN
        assertEquals(0, remoteCallCount)
        assertEquals(ratesForToday, result.getOrThrow())
    }

    @Test
    fun `GIVEN local cache with stale rates WHEN getExchangeRates THEN re-fetches from remote`() = runTest {
        // GIVEN
        var remoteCallCount = 0
        val remote = object : ExchangeRateDataSource {
            override suspend fun getExchangeRates(base: Currency): Result<ExchangeRates> {
                remoteCallCount++
                return Result.success(ratesForToday)
            }
        }
        val local = object : LocalExchangeRateDataSource {
            override suspend fun get(base: Currency) = ratesForYesterday // stale
            override suspend fun save(rates: ExchangeRates) = Unit
        }
        // WHEN
        val result = buildRepo(remote, local).getExchangeRates(Currency.EUR)
        // THEN
        assertEquals(1, remoteCallCount)
        assertEquals(ratesForToday, result.getOrThrow())
    }

    @Test
    fun `GIVEN remote error and no cache WHEN getExchangeRates THEN returns failure`() = runTest {
        // GIVEN
        val error = RuntimeException("API error")
        val remote = object : ExchangeRateDataSource {
            override suspend fun getExchangeRates(base: Currency) = Result.failure<ExchangeRates>(error)
        }
        // WHEN
        val result = buildRepo(remote).getExchangeRates(Currency.EUR)
        // THEN
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `GIVEN remote success WHEN getExchangeRates THEN persists to local datasource`() = runTest {
        // GIVEN
        val remote = object : ExchangeRateDataSource {
            override suspend fun getExchangeRates(base: Currency) = Result.success(ratesForToday)
        }
        var savedRates: ExchangeRates? = null
        val local = object : LocalExchangeRateDataSource {
            override suspend fun get(base: Currency) = null
            override suspend fun save(rates: ExchangeRates) { savedRates = rates }
        }
        // WHEN
        buildRepo(remote, local).getExchangeRates(Currency.EUR)
        // THEN
        assertEquals(ratesForToday, savedRates)
    }
}
