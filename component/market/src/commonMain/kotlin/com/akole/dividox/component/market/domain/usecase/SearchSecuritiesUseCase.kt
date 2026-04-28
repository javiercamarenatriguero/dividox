package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.repository.MarketRepository

class SearchSecuritiesUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(query: String): Result<List<StockQuote>> =
        repository.searchSecurities(query)
}
