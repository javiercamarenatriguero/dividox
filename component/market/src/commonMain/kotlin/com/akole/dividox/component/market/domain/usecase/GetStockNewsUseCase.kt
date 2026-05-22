package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.NewsItem
import com.akole.dividox.component.market.domain.repository.MarketRepository

class GetStockNewsUseCase(private val repository: MarketRepository) {

    suspend operator fun invoke(ticker: String, exchange: String? = null, count: Int = 10): Result<List<NewsItem>> {
        val (lang, region) = exchangeToLangRegion(exchange)
        // Fetch more than needed so we can filter by relatedTickers and still hit count
        return repository.getNews(ticker, count = FETCH_MULTIPLIER * count, lang = lang, region = region, filterTicker = ticker)
            .map { it.take(count) }
    }

    private fun exchangeToLangRegion(exchange: String?): Pair<String, String> {
        val e = exchange?.uppercase() ?: return "en" to "US"
        return when {
            e.contains("BME") || e.contains("MCE") || e.contains("MAD") -> "es" to "ES"
            e.contains("LSE") -> "en" to "GB"
            e.contains("XETRA") || e.contains("FSX") || e.contains("FRA") -> "de" to "DE"
            e.contains("EURONEXT") || e.contains("PAR") -> "fr" to "FR"
            e.contains("AMS") -> "nl" to "NL"
            e.contains("MIL") -> "it" to "IT"
            e.contains("TSE") || e.contains("OSE") -> "ja" to "JP"
            e.contains("SIX") || e.contains("SWX") -> "de" to "CH"
            e.contains("TSX") -> "en" to "CA"
            e.contains("ASX") -> "en" to "AU"
            else -> "en" to "US"
        }
    }

    private companion object {
        const val FETCH_MULTIPLIER = 5
    }
}
