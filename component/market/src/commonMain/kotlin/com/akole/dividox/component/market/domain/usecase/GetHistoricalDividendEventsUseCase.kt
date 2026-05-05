package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.component.market.domain.model.MarketDividendEvent
import com.akole.dividox.component.market.domain.repository.MarketRepository

class GetHistoricalDividendEventsUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(
        ticker: String,
        range: DividendHistoryRange = DividendHistoryRange.MAX,
    ): Result<List<MarketDividendEvent>> = repository.getHistoricalDividendEvents(ticker, range)
}
