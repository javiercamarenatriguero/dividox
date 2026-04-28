package com.akole.dividox.component.portfolio.domain.usecase

import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository

/**
 * Update an existing share holding in user's portfolio.
 */
class UpdateHoldingUseCase(private val repository: PortfolioRepository) {
    suspend fun execute(holding: Holding): Result<Unit> =
        repository.updateHolding(holding)
}
