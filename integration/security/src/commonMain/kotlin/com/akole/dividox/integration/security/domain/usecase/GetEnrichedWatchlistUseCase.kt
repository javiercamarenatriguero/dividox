package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.component.market.domain.usecase.GetCompanyInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.component.watchlist.domain.usecase.GetWatchlistUseCase
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Combines the watchlist and portfolio into a list of [EnrichedWatchlistEntry] values.
 *
 * For each watchlist emission:
 * 1. Fetches quotes in batch via [GetMultipleQuotesUseCase].
 * 2. Fetches company info per ticker (failures silently produce null).
 * 3. Cross-checks each entry against the latest portfolio snapshot.
 *
 * Emits whenever the watchlist **or** the portfolio changes.
 */
class GetEnrichedWatchlistUseCase(
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val getMultipleQuotesUseCase: GetMultipleQuotesUseCase,
    private val getCompanyInfoUseCase: GetCompanyInfoUseCase,
    private val getPortfolioUseCase: GetPortfolioUseCase,
) {
    operator fun invoke(): Flow<List<EnrichedWatchlistEntry>> =
        combine(
            getWatchlistUseCase(),
            getPortfolioUseCase.execute(),
        ) { watchlistEntries, portfolioResult ->
            if (watchlistEntries.isEmpty()) return@combine emptyList()

            val holdings = portfolioResult.getOrElse { emptyList() }
            val portfolioTickers = holdings.map { it.tickerId }.toSet()

            val tickers = watchlistEntries.map { it.tickerId }
            val quotes = getMultipleQuotesUseCase(tickers)
                .getOrElse { emptyList() }
            val quoteByTicker = quotes.associateBy { it.ticker }

            watchlistEntries.map { entry ->
                val quote = quoteByTicker[entry.tickerId]
                val companyInfo = getCompanyInfoUseCase(entry.tickerId).getOrNull()
                EnrichedWatchlistEntry(
                    entry = entry,
                    quote = quote,
                    companyInfo = companyInfo,
                    isInPortfolio = entry.tickerId in portfolioTickers,
                )
            }
        }
}
