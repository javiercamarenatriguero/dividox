package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.usecase.GetCompanyInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.component.watchlist.domain.usecase.GetWatchlistUseCase
import com.akole.dividox.integration.security.domain.model.EnrichedWatchlistEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Combines the watchlist and portfolio into a list of [EnrichedWatchlistEntry] values.
 *
 * For each watchlist/portfolio emission:
 * 1. Emits immediately with cached (possibly stale) quotes, if any.
 * 2. Fetches fresh quotes in batch via [GetMultipleQuotesUseCase] while company info is
 *    resolved per ticker in parallel (failures silently produce null).
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
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<EnrichedWatchlistEntry>> =
        combine(
            getWatchlistUseCase(),
            getPortfolioUseCase.execute(),
        ) { watchlistEntries, portfolioResult -> watchlistEntries to portfolioResult }
            .flatMapLatest { (watchlistEntries, portfolioResult) ->
                if (watchlistEntries.isEmpty()) return@flatMapLatest flowOf(emptyList())

                val holdings = portfolioResult.getOrElse { emptyList() }
                val portfolioTickers = holdings.map { it.tickerId }.toSet()
                val tickers = watchlistEntries.map { it.tickerId }

                flow {
                    coroutineScope {
                        // Company info (Room-cached for 7 days) is fetched in parallel with quotes.
                        val companyByTicker = tickers.distinct().associateWith { ticker ->
                            async { getCompanyInfoUseCase(ticker).getOrNull() }
                        }

                        suspend fun build(quotes: List<StockQuote>): List<EnrichedWatchlistEntry> {
                            val quoteByTicker = quotes.associateBy { it.ticker }
                            return watchlistEntries.map { entry ->
                                EnrichedWatchlistEntry(
                                    entry = entry,
                                    quote = quoteByTicker[entry.tickerId],
                                    companyInfo = companyByTicker[entry.tickerId]?.await(),
                                    isInPortfolio = entry.tickerId in portfolioTickers,
                                )
                            }
                        }

                        // Stale-while-revalidate: paint cached quotes before the network returns.
                        val stale = getMultipleQuotesUseCase.cached(tickers)
                        if (stale.isNotEmpty()) emit(build(stale))

                        val fresh = getMultipleQuotesUseCase(tickers).getOrElse { emptyList() }
                        // Fresh quotes win; tickers whose refresh failed keep their stale quote.
                        if (stale.isEmpty() || fresh.isNotEmpty()) emit(build(stale + fresh))
                    }
                }
            }
}
