package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.repository.MarketRepository

class GetStockQuoteUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(ticker: String): Result<StockQuote> =
        repository.getStockQuote(ticker)
}
