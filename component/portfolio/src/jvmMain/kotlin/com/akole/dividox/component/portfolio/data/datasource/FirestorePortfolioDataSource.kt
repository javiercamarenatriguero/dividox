package com.akole.dividox.component.portfolio.data.datasource

import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * JVM stub—Firestore available but desktop app uses local storage MVP.
 * Returns empty portfolio. Wire Firestore emulator for local dev later.
 */
class FirestorePortfolioDataSource(
    private val userId: String,
) : PortfolioDataSource {
    override fun observePortfolio(): Flow<Result<List<Holding>>> =
        flowOf(Result.success(emptyList()))

    override suspend fun getPortfolio(): Result<List<Holding>> =
        Result.success(emptyList())

    override suspend fun addHolding(holding: Holding): Result<HoldingId> =
        Result.failure(Exception("Desktop Firestore not configured"))

    override suspend fun updateHolding(holding: Holding): Result<Unit> =
        Result.failure(Exception("Desktop Firestore not configured"))

    override suspend fun removeHolding(holdingId: HoldingId): Result<Unit> =
        Result.failure(Exception("Desktop Firestore not configured"))
}
