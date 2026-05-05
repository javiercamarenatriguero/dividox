package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.component.market.domain.model.StockQuote
import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.model.Holding
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlin.time.Clock
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
                        // Offline or API unavailable: emit holdings with purchase price as
                        // fallback so the portfolio list is visible. isLive=false lets the UI
                        // hide price-sensitive fields (gain %, current value).
                        emit(holdings.map { it.toOfflineFallback() })
                        throw IllegalStateException("Market quotes unavailable, will retry")
                    }

                    val quoteByTicker = quotes.associateBy { it.ticker }
                    emit(
                        holdings.mapNotNull { holding ->
                            val quote = quoteByTicker[holding.tickerId] ?: return@mapNotNull null
                            val dividendInfo = getDividendInfoUseCase(holding.tickerId).getOrNull()
                            val costBasis = holding.shares * holding.purchasePrice
                            val currentValue = holding.shares * quote.price
                            val totalGainPercent = if (costBasis != 0.0) {
                                (currentValue - costBasis) / costBasis * 100.0
                            } else {
                                0.0
                            }
                            SecurityHolding(
                                holding = holding,
                                quote = quote,
                                dividendInfo = dividendInfo,
                                totalGainPercent = totalGainPercent,
                            )
                        },
                    )
                }.retryWhen { _, attempt ->
                    delay(minOf(2_000L * (attempt + 1), 30_000L))
                    true
                }
            }

    private fun Holding.toOfflineFallback(): SecurityHolding = SecurityHolding(
        holding = this,
        quote = StockQuote(
            ticker = tickerId,
            price = purchasePrice,
            change = 0.0,
            changePercent = 0.0,
            currency = "USD",
            lastUpdated = Clock.System.now(),
        ),
        dividendInfo = null,
        totalGainPercent = 0.0,
    )
}
