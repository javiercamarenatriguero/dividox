package com.akole.dividox.component.portfolio.data.repository

import com.akole.dividox.component.portfolio.data.datasource.PortfolioDataSource
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository implementation that delegates to PortfolioDataSource.
 * Ensures all I/O operations run on [ioDispatcher].
 */
class PortfolioRepositoryImpl(
    private val dataSource: PortfolioDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : PortfolioRepository {
    override fun observePortfolio(): Flow<Result<List<Holding>>> =
        dataSource.observePortfolio().flowOn(ioDispatcher)

    override suspend fun getPortfolio(): Result<List<Holding>> =
        withContext(ioDispatcher) {
            dataSource.getPortfolio()
        }

    override suspend fun addHolding(holding: Holding): Result<HoldingId> =
        withContext(ioDispatcher) {
            dataSource.addHolding(holding)
        }

    override suspend fun updateHolding(holding: Holding): Result<Unit> =
        withContext(ioDispatcher) {
            dataSource.updateHolding(holding)
        }

    override suspend fun removeHolding(holdingId: HoldingId): Result<Unit> =
        withContext(ioDispatcher) {
            dataSource.removeHolding(holdingId)
        }
}
