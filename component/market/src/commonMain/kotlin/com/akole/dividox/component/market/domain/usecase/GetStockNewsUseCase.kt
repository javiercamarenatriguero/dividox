package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.NewsItem
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.component.market.platform.deviceLanguage
import com.akole.dividox.component.market.platform.deviceRegion

class GetStockNewsUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(ticker: String, count: Int = 3): Result<List<NewsItem>> =
        repository.getNews(ticker, count, lang = deviceLanguage(), region = deviceRegion())
}
