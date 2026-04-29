package com.akole.dividox.integration.security

import com.akole.dividox.component.portfolio.domain.model.Currency
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.model.HoldingId
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePortfolioRepository : PortfolioRepository {

    private val holdingsFlow = MutableStateFlow<Result<List<Holding>>>(Result.success(emptyList()))

    fun setHoldings(holdings: List<Holding>) {
        holdingsFlow.value = Result.success(holdings)
    }

    fun setError(error: Throwable) {
        holdingsFlow.value = Result.failure(error)
    }

    override fun observePortfolio(): Flow<Result<List<Holding>>> = holdingsFlow

    override suspend fun getPortfolio(): Result<List<Holding>> = holdingsFlow.value

    override suspend fun addHolding(holding: Holding): Result<HoldingId> =
        Result.success(holding.id)

    override suspend fun updateHolding(holding: Holding): Result<Unit> = Result.success(Unit)

    override suspend fun removeHolding(holdingId: HoldingId): Result<Unit> = Result.success(Unit)

    companion object {

        fun holding(
            id: String = "h1",
            tickerId: String = "AAPL",
            shares: Double = 10.0,
            purchasePrice: Double = 100.0,
            currency: Currency = Currency.USD,
            purchaseDate: Long = 0L,
        ) = Holding(
            id = HoldingId(id),
            tickerId = tickerId,
            shares = shares,
            purchasePrice = purchasePrice,
            purchaseCurrency = currency,
            purchaseDate = purchaseDate,
        )
    }
}
