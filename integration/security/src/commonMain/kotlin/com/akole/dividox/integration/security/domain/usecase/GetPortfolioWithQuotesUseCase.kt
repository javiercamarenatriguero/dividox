package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetMultipleQuotesUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Combines portfolio holdings with live market quotes and dividend data.
 *
 * For each emission from [GetPortfolioUseCase]:
 * 1. Fetches all quotes in a single batch call via [GetMultipleQuotesUseCase].
 * 2. Fetches dividend info per ticker (failures are silently absorbed as null).
 * 3. Computes [SecurityHolding.totalGainPercent] for each holding.
 *
 * Holdings for which no quote is available are excluded from the result.
 */
class GetPortfolioWithQuotesUseCase(
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val getMultipleQuotesUseCase: GetMultipleQuotesUseCase,
    private val getDividendInfoUseCase: GetDividendInfoUseCase,
) {
    operator fun invoke(): Flow<List<SecurityHolding>> =
        getPortfolioUseCase.execute().map { portfolioResult ->
            val holdings = portfolioResult.getOrElse { return@map emptyList() }
            if (holdings.isEmpty()) return@map emptyList()

            val tickers = holdings.map { it.tickerId }
            val quotes = getMultipleQuotesUseCase(tickers)
                .getOrElse { return@map emptyList() }
            val quoteByTicker = quotes.associateBy { it.ticker }

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
            }
        }
}
