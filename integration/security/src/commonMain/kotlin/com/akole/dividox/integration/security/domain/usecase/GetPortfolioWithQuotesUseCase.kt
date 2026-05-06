package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.common.currency.CurrencyConverter
import com.akole.dividox.common.currency.domain.model.Currency
import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.retryWhen

/**
 * Combines portfolio holdings with live market quotes and dividend data.
 *
 * For each emission from [GetPortfolioUseCase]:
 * 1. Fetches all quotes in a single batch call via [GetMultipleQuotesUseCase].
 * 2. Fetches dividend info per ticker (failures are silently absorbed as null).
 * 3. Computes [SecurityHolding.totalGainPercent] for each holding.
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
    operator fun invoke(): Flow<List<SecurityHolding>> =
        getPortfolioUseCase.execute()
            .flatMapLatest { portfolioResult ->
                val holdings = portfolioResult.getOrElse { return@flatMapLatest flowOf(emptyList()) }
                if (holdings.isEmpty()) return@flatMapLatest flowOf(emptyList())

                flow {
                    val tickers = holdings.map { it.tickerId }
                    val quotes = getMultipleQuotesUseCase(tickers).getOrNull()

                    if (quotes == null || (quotes.isEmpty() && tickers.isNotEmpty())) {
                        // Don't emit stale/fallback data — keep skeleton visible until real
                        // quotes arrive. retryWhen handles back-off and re-fetch.
                        throw IllegalStateException("Market quotes unavailable, will retry")
                    }

                    val quoteByTicker = quotes.associateBy { it.ticker }
                    val holdingsWithQuotes = holdings.filter { it.tickerId in quoteByTicker }
                    emit(
                        coroutineScope {
                            holdingsWithQuotes.map { holding ->
                                async {
                                    val quote = quoteByTicker[holding.tickerId]!!
                                    val dividendInfo = getDividendInfoUseCase(holding.tickerId).getOrNull()
                                    // Normalize purchase price to quote.currency so gain% is
                                    // comparable regardless of the currency the user bought in.
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
                                        dividendInfo = dividendInfo,
                                        totalGainPercent = totalGainPercent,
                                    )
                                }
                            }.map { it.await() }
                        },
                    )
                }.retryWhen { _, attempt ->
                    delay(minOf(2_000L * (attempt + 1), 30_000L))
                    true
                }
            }

}
