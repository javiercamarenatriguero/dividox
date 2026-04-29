package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.integration.security.domain.model.SecurityHolding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Returns a [SecurityHolding] for a specific ticker if the user holds it, or null otherwise.
 *
 * Reacts to portfolio changes — emits null when the holding is removed.
 */
class GetSecurityHoldingUseCase(
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val getStockQuoteUseCase: GetStockQuoteUseCase,
    private val getDividendInfoUseCase: GetDividendInfoUseCase,
) {
    operator fun invoke(ticker: String): Flow<SecurityHolding?> =
        getPortfolioUseCase.execute().map { portfolioResult ->
            val holdings = portfolioResult.getOrElse { return@map null }
            val holding = holdings.firstOrNull { it.tickerId == ticker } ?: return@map null

            val quote = getStockQuoteUseCase(ticker).getOrElse { return@map null }
            val dividendInfo = getDividendInfoUseCase(ticker).getOrNull()

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
