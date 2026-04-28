package com.akole.dividox.component.portfolio.domain.usecase

import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository

/**
 * Add a new share holding to user's portfolio.
 *
 * @return Result with generated HoldingId or error
 */
class AddHoldingUseCase(private val repository: PortfolioRepository) {
    suspend fun execute(holding: Holding): Result<HoldingId> =
        repository.addHolding(holding)
}
