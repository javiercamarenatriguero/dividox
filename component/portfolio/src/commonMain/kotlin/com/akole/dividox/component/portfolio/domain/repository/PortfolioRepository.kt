package com.akole.dividox.component.portfolio.domain.repository

import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import kotlinx.coroutines.flow.Flow

/**
 * Repository for user's portfolio (share holdings). Persisted in Firestore.
 * Does not include live market data—use `:component:market` for prices/dividends.
 */
interface PortfolioRepository {
    /**
     * Observe portfolio as a reactive stream. Emits whenever holdings change.
     *
     * @return Flow<Result<List<Holding>>> where failure indicates Firestore read error
     */
    fun observePortfolio(): Flow<Result<List<Holding>>>

    /**
     * Fetch current portfolio snapshot (one-shot read).
     *
     * @return Result with holdings or error
     */
    suspend fun getPortfolio(): Result<List<Holding>>

    /**
     * Add a new holding to portfolio.
     *
     * @param holding share to add (id field may be ignored if backend generates it)
     * @return Result with generated HoldingId or error
     */
    suspend fun addHolding(holding: Holding): Result<HoldingId>

    /**
     * Update an existing holding.
     *
     * @param holding updated holding (id field identifies the record to update)
     * @return Result.success on update or error
     */
    suspend fun updateHolding(holding: Holding): Result<Unit>

    /**
     * Remove a holding from portfolio.
     *
     * @param holdingId ID of holding to delete
     * @return Result.success on delete or error
     */
    suspend fun removeHolding(holdingId: HoldingId): Result<Unit>
}
