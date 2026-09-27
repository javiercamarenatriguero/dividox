package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.component.market.domain.model.DividendInfo
import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.retryWhen

/**
 * Combines portfolio holdings with live market quotes and dividend data.
 *
 * For each emission from [GetPortfolioUseCase]:
 * 1. Emits immediately from cached (possibly stale) quotes, if any.
 * 2. Fetches fresh quotes in a batch and dividend info per ticker **in parallel**
 *    (dividend failures are silently absorbed as null).
 * 3. Computes [SecurityHolding.totalGainPercent] for each holding.
 *
 * Emits per snapshot: stale (if cached) → fresh quotes → enriched (only if dividends were pending).
 * Dividend info is remembered across snapshots, see [SecurityHolding.isDividendInfoResolved].
 *
 * If the quotes API fails (e.g. network error or rate-limit on fresh install), the inner
 * flow retries with exponential back-off instead of silently returning empty. This prevents
 * the portfolio appearing permanently empty after the first Firestore snapshot when the
 * market API is temporarily unavailable.
 *
 * Holdings for which no quote is available are excluded from the result.
 */
class GetPortfolioWithQuotesUseCase(
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val getMultipleQuotesUseCase: GetMultipleQuotesUseCase,
    private val getDividendInfoUseCase: GetDividendInfoUseCase,
    private val currencyConverter: CurrencyConverter,
) {
    operator fun invoke(): Flow<List<SecurityHolding>> = flow {
        // Dividend info resolved in earlier snapshots of this collection. Firestore emits several
        // snapshots on cold start (local cache → server); carrying this over prevents yield from
        // dropping back to "pending"/0% each time the portfolio re-emits.
        val knownDividends = mutableMapOf<String, DividendInfo?>()

        emitAll(
            getPortfolioUseCase.execute().flatMapLatest { portfolioResult ->
                val holdings = portfolioResult.getOrElse { return@flatMapLatest flowOf(emptyList()) }
                if (holdings.isEmpty()) return@flatMapLatest flowOf(emptyList())

                // Stale data is painted only once per portfolio snapshot, not on every retry.
                var staleEmitted = false
                flow {
                    val tickers = holdings.map { it.tickerId }

                    // Phase 0 (stale-while-revalidate): paint instantly from any cached quotes,
                    // even expired ones, so cold start never waits on the network.
                    if (!staleEmitted) {
                        val stale = getMultipleQuotesUseCase.cached(tickers)
                        val staleHoldings = buildHoldings(holdings, stale, knownDividends)
                        if (staleHoldings.isNotEmpty()) {
                            emit(staleHoldings)
                            staleEmitted = true
                        }
                    }

                    coroutineScope {
                        // Dividend info is fetched in parallel with quotes (not after them).
                        val dividendByTicker = tickers.distinct()
                            .filterNot { it in knownDividends }
                            .associateWith { ticker -> async { getDividendInfoUseCase(ticker).getOrNull() } }

                        val quotes = getMultipleQuotesUseCase(tickers).getOrNull()
                        if (quotes == null || (quotes.isEmpty() && tickers.isNotEmpty())) {
                            // Don't emit fallback data — keep skeleton (or stale data) visible
                            // until real quotes arrive. retryWhen handles back-off and re-fetch.
                            throw IllegalStateException("Market quotes unavailable, will retry")
                        }

                        // Phase 1: fresh quotes, with any dividend info already known. Tickers whose
                        // refresh failed keep their last cached quote instead of silently dropping
                        // out of the portfolio (which made Total Value dip and recover).
                        val missing = tickers.toSet() - quotes.map { it.ticker }.toSet()
                        val fallback = if (missing.isEmpty()) emptyList() else getMultipleQuotesUseCase.cached(missing.toList())
                        val initialHoldings = buildHoldings(holdings, fallback + quotes, knownDividends)
                        emit(initialHoldings)

                        // Phase 2: enrich remaining holdings with dividend info and re-emit.
                        if (dividendByTicker.isNotEmpty()) {
                            dividendByTicker.forEach { (ticker, deferred) -> knownDividends[ticker] = deferred.await() }
                            emit(
                                initialHoldings.map { sh ->
                                    sh.copy(
                                        dividendInfo = knownDividends[sh.holding.tickerId],
                                        isDividendInfoResolved = true,
                                    )
                                },
                            )
                        }
                    }
                }.retryWhen { _, attempt ->
                    delay(minOf(2_000L * (attempt + 1), 30_000L))
                    true
                }
            },
        )
    }

    private suspend fun buildHoldings(
        holdings: List<Holding>,
        quotes: List<StockQuote>,
        knownDividends: Map<String, DividendInfo?>,
    ): List<SecurityHolding> {
        val quoteByTicker = quotes.associateBy { it.ticker }
        return holdings.mapNotNull { holding ->
            val quote = quoteByTicker[holding.tickerId] ?: return@mapNotNull null
            val quoteCurrency = Currency.entries
                .firstOrNull { it.code == quote.currency } ?: Currency.USD
            val purchasePriceNorm = currencyConverter.convert(
                holding.purchasePrice,
                holding.purchaseCurrency,
                quoteCurrency,
            ).getOrElse { holding.purchasePrice }
            val totalGainPercent = if (purchasePriceNorm != 0.0) {
                (quote.price - purchasePriceNorm) / purchasePriceNorm * 100.0
            } else {
                0.0
            }
            SecurityHolding(
                holding = holding,
                quote = quote,
                dividendInfo = knownDividends[holding.tickerId],
                totalGainPercent = totalGainPercent,
                isDividendInfoResolved = holding.tickerId in knownDividends,
            )
        }
    }
}
