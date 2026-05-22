package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.NewsItem
import com.akole.dividox.component.market.domain.repository.MarketRepository

class GetMarketNewsUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(defaultMarket: String, count: Int = 10): Result<List<NewsItem>> =
        repository.getNews(defaultMarketToIndex(defaultMarket), count)

    private fun defaultMarketToIndex(market: String): String = when (market) {
        "ES" -> "^IBEX"
        "EU" -> "^STOXX50E"
        "DE" -> "^GDAXI"
        "UK" -> "^FTSE"
        "JP" -> "^N225"
        else -> "^GSPC"
    }
}
