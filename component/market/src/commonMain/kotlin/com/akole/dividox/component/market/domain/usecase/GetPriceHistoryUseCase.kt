package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.model.PricePoint
import com.akole.dividox.component.market.domain.repository.MarketRepository
import kotlinx.coroutines.flow.Flow

class GetPriceHistoryUseCase(private val repository: MarketRepository) {
    operator fun invoke(ticker: String, period: ChartPeriod): Flow<List<PricePoint>> =
        repository.getPriceHistory(ticker, period)
}
