package com.akole.dividox.component.portfolio.data.datasource

import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlinx.coroutines.flow.Flow

/**
 * Data source abstraction for portfolio persistence layer.
 * Implementation accesses Firestore via platform-specific expect/actual.
 */
interface PortfolioDataSource {
    fun observePortfolio(): Flow<Result<List<Holding>>>
    suspend fun getPortfolio(): Result<List<Holding>>
    suspend fun addHolding(holding: Holding): Result<HoldingId>
    suspend fun updateHolding(holding: Holding): Result<Unit>
    suspend fun removeHolding(holdingId: HoldingId): Result<Unit>
}
