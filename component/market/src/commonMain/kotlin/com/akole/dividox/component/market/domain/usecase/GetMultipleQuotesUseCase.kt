package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.repository.MarketRepository

class GetMultipleQuotesUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(tickers: List<String>): Result<List<StockQuote>> =
        repository.getMultipleQuotes(tickers)
}
