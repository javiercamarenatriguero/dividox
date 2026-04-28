package com.akole.dividox.component.portfolio.domain.usecase

import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository

/**
 * Remove a share holding from user's portfolio.
 */
class RemoveHoldingUseCase(private val repository: PortfolioRepository) {
    suspend fun execute(holdingId: HoldingId): Result<Unit> =
        repository.removeHolding(holdingId)
}
