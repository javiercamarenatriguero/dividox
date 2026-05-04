package com.akole.dividox.integration.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.component.portfolio.domain.model.Holding
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Syncs historical dividend payments from the market API into the user's [DividendRepository].
 *
 * For each ticker in the portfolio, all historical dividend events are fetched and persisted
 * as [DividendPayment] records using the current total share count for that ticker.
 *
 * **Eligibility — earliest purchase date per ticker:**
 * Only events with `exDividendDate >= earliest purchase date` for that ticker are stored.
 * Dividends that occurred before the user ever held the stock are excluded — there is no
 * point showing activity the user did not participate in.
 * Share counts are the total current shares (buy-and-hold MVP, no per-lot tracking).
 *
 * **Re-sync behaviour:**
 * If a user adds an older holding for a ticker (earlier purchase date), re-syncing will
 * populate the previously-excluded historical dividends automatically.
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
 * @param marketRepository Source of per-ticker dividend event history.
 * @param dividendRepository Destination for the computed [DividendPayment] records.
 */
class SyncDividendHistoryFromHoldingsUseCase(
    private val marketRepository: MarketRepository,
    private val dividendRepository: DividendRepository,
) {
    suspend operator fun invoke(holdings: List<Holding>): Result<Unit> = runCatching {
        if (holdings.isEmpty()) return@runCatching

        val holdingsByTicker = holdings.groupBy { it.tickerId }
        val eligiblePayments = mutableListOf<DividendPayment>()

        for ((ticker, tickerHoldings) in holdingsByTicker) {
            val events = marketRepository.getHistoricalDividendEvents(
                ticker = ticker,
                range = DividendHistoryRange.MAX,
            ).getOrNull() ?: continue

            val totalShares = tickerHoldings.sumOf { it.shares }
            if (totalShares <= 0.0) continue

            // Only include dividends from the date the user first held this ticker onwards
            val earliestPurchaseDate = Instant
                .fromEpochMilliseconds(tickerHoldings.minOf { it.purchaseDate })
                .toLocalDateTime(TimeZone.UTC)
                .date

            for (event in events) {
                // Must own the stock strictly before the ex-dividend date to be eligible
                if (event.exDividendDate <= earliestPurchaseDate) continue

                eligiblePayments += DividendPayment(
                    id = DividendPaymentId("$ticker-${event.exDividendDate}"),
                    tickerId = ticker,
                    amount = event.amountPerShare * totalShares,
                    currency = event.currency,
                    paymentDate = event.exDividendDate,
                )
            }
        }

        // Replace the entire cache — removes any stale pre-purchase dividends
        dividendRepository.replaceAllPayments(eligiblePayments)
    }
}
