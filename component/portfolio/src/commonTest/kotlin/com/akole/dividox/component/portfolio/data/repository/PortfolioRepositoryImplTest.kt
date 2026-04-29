package com.akole.dividox.component.portfolio.data.repository

import com.akole.dividox.component.portfolio.data.datasource.PortfolioDataSource
import com.akole.dividox.common.ui.resources.Currency
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PortfolioRepositoryImplTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeDataSource = FakePortfolioDataSource()
    private val repository = PortfolioRepositoryImpl(fakeDataSource, testDispatcher)

    @Test
    fun getPortfolio_returns_data_on_success() = runTest(testDispatcher) {
        // GIVEN
        val holdings = listOf(
            Holding(
                id = HoldingId("1"),
                tickerId = "AAPL",
                shares = 10.0,
                purchasePrice = 150.0,
                purchaseCurrency = Currency.USD,
                purchaseDate = 1000L,
            ),
        )
        fakeDataSource.getResult = Result.success(holdings)

        // WHEN
        val result = repository.getPortfolio()

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(holdings, result.getOrNull())
    }

    @Test
    fun addHolding_returns_generated_id() = runTest(testDispatcher) {
        // GIVEN
        val holding = Holding(
            id = HoldingId("temp"),
            tickerId = "MSFT",
            shares = 5.0,
            purchasePrice = 300.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 2000L,
        )
        val generatedId = HoldingId("gen-id-456")
        fakeDataSource.addResult = Result.success(generatedId)

        // WHEN
        val result = repository.addHolding(holding)

        // THEN
        assertTrue(result.isSuccess)
        assertEquals(generatedId, result.getOrNull())
    }

    @Test
    fun updateHolding_succeeds() = runTest(testDispatcher) {
        // GIVEN
        val holding = Holding(
            id = HoldingId("1"),
            tickerId = "GOOGL",
            shares = 20.0,
            purchasePrice = 140.0,
            purchaseCurrency = Currency.USD,
            purchaseDate = 4000L,
        )
        fakeDataSource.updateResult = Result.success(Unit)

        // WHEN
        val result = repository.updateHolding(holding)

        // THEN
        assertTrue(result.isSuccess)
    }

    @Test
    fun removeHolding_succeeds() = runTest(testDispatcher) {
        // GIVEN
        val holdingId = HoldingId("1")
        fakeDataSource.removeResult = Result.success(Unit)

        // WHEN
        val result = repository.removeHolding(holdingId)

        // THEN
        assertTrue(result.isSuccess)
    }

    @Test
    fun observePortfolio_delegates_to_datasource() = runTest(testDispatcher) {
        // GIVEN
        val holdings = listOf(
            Holding(
                id = HoldingId("1"),
                tickerId = "TSLA",
                shares = 3.0,
                purchasePrice = 250.0,
                purchaseCurrency = Currency.USD,
                purchaseDate = 5000L,
            ),
        )
        fakeDataSource.observeResult = Result.success(holdings)

        // WHEN
        val results = repository.observePortfolio().toList()

        // THEN
        assertEquals(1, results.size)
        assertTrue(results[0].isSuccess)
        assertEquals(holdings, results[0].getOrNull())
    }
}

class FakePortfolioDataSource : PortfolioDataSource {
    var getResult: Result<List<Holding>> = Result.success(emptyList())
    var addResult: Result<HoldingId> = Result.success(HoldingId("default"))
    var updateResult: Result<Unit> = Result.success(Unit)
    var removeResult: Result<Unit> = Result.success(Unit)
    var observeResult: Result<List<Holding>> = Result.success(emptyList())

    override fun observePortfolio() = flowOf<Result<List<Holding>>>(observeResult)
    override suspend fun getPortfolio(): Result<List<Holding>> = getResult
    override suspend fun addHolding(holding: Holding): Result<HoldingId> = addResult
    override suspend fun updateHolding(holding: Holding): Result<Unit> = updateResult
    override suspend fun removeHolding(holdingId: HoldingId): Result<Unit> = removeResult
}
