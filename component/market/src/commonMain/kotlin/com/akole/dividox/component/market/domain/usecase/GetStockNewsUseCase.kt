package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.NewsItem
import com.akole.dividox.component.market.domain.repository.MarketRepository

class GetStockNewsUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(ticker: String, count: Int = 10): Result<List<NewsItem>> =
        repository.getNews(ticker, count)
}
