package com.akole.dividox.component.portfolio.domain.usecase

import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe user's portfolio holdings as a reactive stream.
 * Emits every time holdings are added, updated, or removed.
 */
class GetPortfolioUseCase(private val repository: PortfolioRepository) {
    fun execute(): Flow<Result<List<Holding>>> = repository.observePortfolio()
}
