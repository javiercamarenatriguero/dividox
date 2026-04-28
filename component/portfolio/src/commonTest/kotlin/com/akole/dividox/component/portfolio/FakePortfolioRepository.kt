package com.akole.dividox.component.portfolio

import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.flowOf

class FakePortfolioRepository : PortfolioRepository {
    var portfolioResult: Result<List<Holding>> = Result.success(emptyList())
    var addResult: Result<HoldingId> = Result.success(HoldingId("default"))
    var updateResult: Result<Unit> = Result.success(Unit)
    var removeResult: Result<Unit> = Result.success(Unit)

    override fun observePortfolio() = flowOf<Result<List<Holding>>>(portfolioResult)
    override suspend fun getPortfolio(): Result<List<Holding>> = portfolioResult
    override suspend fun addHolding(holding: Holding) = addResult
    override suspend fun updateHolding(holding: Holding) = updateResult
    override suspend fun removeHolding(holdingId: HoldingId) = removeResult
}
