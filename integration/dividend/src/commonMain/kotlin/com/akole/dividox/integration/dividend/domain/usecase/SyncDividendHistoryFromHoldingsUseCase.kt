package com.akole.dividox.integration.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.component.portfolio.domain.repository.PortfolioRepository

/**
 * Syncs historical dividend payments from the market API into the user's [DividendRepository].
 *
 * For each ticker in the portfolio, all historical dividend events are fetched and persisted
 * as [DividendPayment] records using the current total share count for that ticker.
 *
 * **MVP scope — buy-and-hold, no lot tracking:**
 * Share counts are derived from the *current* portfolio snapshot summed per ticker.
 * Purchase date is not used for eligibility — all historical dividends are included.
 * Per-lot tracking (filtering by purchase date) is deferred to a future iteration.
 *
 * **Currency:**
 * Amounts are stored in the security's native trading currency as reported by Yahoo Finance.
 * Multi-currency consolidation is deferred to a future iteration.
 *
 * **Deduplication:**
 * Payment IDs are stable: `"{ticker}-{exDividendDate}"`. Calling this use case multiple
 * times is safe — [DividendRepository.addDividendPayment] uses upsert semantics, so no
 * duplicates are created.
 *
 * @param portfolioRepository Source of the user's current holdings.
 * @param marketRepository Source of per-ticker dividend event history.
 * @param dividendRepository Destination for the computed [DividendPayment] records.
 */
class SyncDividendHistoryFromHoldingsUseCase(
    private val portfolioRepository: PortfolioRepository,
    private val marketRepository: MarketRepository,
    private val dividendRepository: DividendRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val holdings = portfolioRepository.getPortfolio().getOrNull() ?: return@runCatching
        if (holdings.isEmpty()) return@runCatching

        val holdingsByTicker = holdings.groupBy { it.tickerId }

        for ((ticker, tickerHoldings) in holdingsByTicker) {
            val events = marketRepository.getHistoricalDividendEvents(
                ticker = ticker,
                range = DividendHistoryRange.MAX,
            ).getOrNull() ?: continue

            // Total current shares for this ticker (buy-and-hold MVP: no per-lot tracking)
            val totalShares = tickerHoldings.sumOf { it.shares }
            if (totalShares <= 0.0) continue

            for (event in events) {
                val paymentId = DividendPaymentId("$ticker-${event.exDividendDate}")
                dividendRepository.addDividendPayment(
                    DividendPayment(
                        id = paymentId,
                        tickerId = ticker,
                        amount = event.amountPerShare * totalShares,
                        currency = event.currency,
                        paymentDate = event.exDividendDate,
                    ),
                )
            }
        }
    }
}
