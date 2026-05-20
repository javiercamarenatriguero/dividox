package com.akole.dividox.component.market.domain.usecase

import com.akole.dividox.component.market.domain.model.MARKET_INDICES
import com.akole.dividox.component.market.domain.model.MarketError
import com.akole.dividox.component.market.domain.model.MarketIndexQuote
import com.akole.dividox.component.market.domain.repository.MarketRepository

class GetMajorMarketIndicesUseCase(private val repository: MarketRepository) {
    suspend operator fun invoke(defaultMarket: String): Result<List<MarketIndexQuote>> {
        val tickers = MARKET_INDICES.map { it.ticker }
        val quotesResult = repository.getMultipleQuotes(tickers)

        return quotesResult.mapCatching { quotes ->
            if (quotes.isEmpty()) {
                throw MarketError.Unknown("All market indices failed to fetch")
            }

            val indexMap = MARKET_INDICES.associate { it.ticker to it }
            val indices = quotes.mapNotNull { quote ->
                indexMap[quote.ticker]?.let { entry ->
                    MarketIndexQuote(
                        name = entry.displayName,
                        ticker = quote.ticker,
                        marketKey = entry.marketKey,
                        points = quote.price,
                        changePoints = quote.change,
                        changePercent = quote.changePercent,
                        lastUpdated = quote.lastUpdated,
                    )
                }
            }

            sortByMarket(indices, defaultMarket)
        }
    }

    private fun sortByMarket(
        indices: List<MarketIndexQuote>,
        defaultMarket: String,
    ): List<MarketIndexQuote> {
        if (defaultMarket == "ALL") return indices

        val selectedMarket = indices.filter { it.marketKey == defaultMarket }
        val remaining = indices.filter { it.marketKey != defaultMarket }
        return selectedMarket + remaining
    }
}
