package com.akole.dividox.integration.security.domain.usecase

import com.akole.dividox.component.market.domain.model.ChartPeriod
import com.akole.dividox.component.market.domain.usecase.GetCompanyInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetDividendInfoUseCase
import com.akole.dividox.component.market.domain.usecase.GetPriceHistoryUseCase
import com.akole.dividox.component.market.domain.usecase.GetStockQuoteUseCase
import com.akole.dividox.component.portfolio.domain.usecase.GetPortfolioUseCase
import com.akole.dividox.component.watchlist.domain.usecase.IsInWatchlistUseCase
import com.akole.dividox.integration.security.domain.model.SecurityDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

/**
 * Builds a full [SecurityDetail] for a given ticker and chart period.
 *
 * Combines:
 * - Live quote (required — errors propagate as an exception in the flow).
 * - Dividend info (optional — failure yields null).
 * - Company info (optional — failure yields null).
 * - Price history (from [GetPriceHistoryUseCase]).
 * - Portfolio membership (from [GetPortfolioUseCase]).
 * - Watchlist membership (from [IsInWatchlistUseCase]).
 *
 * The returned flow emits whenever portfolio **or** watchlist state changes.
 * Market data is fetched once per subscription.
 */
class GetSecurityDetailUseCase(
    private val getStockQuoteUseCase: GetStockQuoteUseCase,
    private val getDividendInfoUseCase: GetDividendInfoUseCase,
    private val getCompanyInfoUseCase: GetCompanyInfoUseCase,
    private val getPriceHistoryUseCase: GetPriceHistoryUseCase,
    private val getPortfolioUseCase: GetPortfolioUseCase,
    private val isInWatchlistUseCase: IsInWatchlistUseCase,
) {
    operator fun invoke(ticker: String, period: ChartPeriod): Flow<SecurityDetail> =
        combine(
            portfolioFlow(ticker),
            isInWatchlistUseCase(ticker),
            getPriceHistoryUseCase(ticker, period),
        ) { portfolioResult, isInWatchlist, priceHistory ->
            val quote = getStockQuoteUseCase(ticker).getOrThrow()
            val dividendInfo = getDividendInfoUseCase(ticker).getOrNull()
            val companyInfo = getCompanyInfoUseCase(ticker).getOrNull()

            val holdings = portfolioResult.getOrElse { emptyList() }
            val matchingHolding = holdings.firstOrNull { it.tickerId == ticker }

            SecurityDetail(
                ticker = ticker,
                quote = quote,
                dividendInfo = dividendInfo,
                companyInfo = companyInfo,
                priceHistory = priceHistory,
                isInPortfolio = matchingHolding != null,
                isInWatchlist = isInWatchlist,
                holdingId = matchingHolding?.id,
            )
        }

    private fun portfolioFlow(ticker: String) = flow {
        getPortfolioUseCase.execute().collect { emit(it) }
    }
}
