package com.akole.dividox.integration.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.model.DividendPaymentId
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.market.domain.model.DividendHistoryRange
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.component.portfolio.domain.model.Holding
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Syncs historical dividend payments from the market API into the user's [DividendRepository].
 *
 * For each ticker in the portfolio, all historical dividend events are fetched and persisted
 * as [DividendPayment] records using the current total share count for that ticker.
 *
 * **Eligibility — per-event share count:**
 * For each ex-dividend date, only lots purchased *strictly before* that date are counted.
 * This matches stock market convention (you must own shares before the ex-date to receive
 * the dividend) and produces accurate historical amounts when the user built up their
 * position over time.
 *
 * **Re-sync behaviour:**
 * If a user adds an older holding for a ticker (earlier purchase date), re-syncing will
 * automatically raise the share count for events that fall after that earlier purchase date.
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

        val eligiblePayments = coroutineScope {
            holdingsByTicker.map { (ticker, tickerHoldings) ->
                async {
                    val events = marketRepository.getHistoricalDividendEvents(
                        ticker = ticker,
                        range = DividendHistoryRange.MAX,
                    ).getOrNull() ?: return@async emptyList()

                    events.mapNotNull { event ->
                        // Only count shares from lots purchased strictly before the ex-dividend date.
                        // Stock market convention: you must own the stock before ex-date to receive it.
                        val sharesOnExDate = tickerHoldings
                            .filter { holding ->
                                Instant.fromEpochMilliseconds(holding.purchaseDate)
                                    .toLocalDateTime(TimeZone.UTC)
                                    .date < event.exDividendDate
                            }
                            .sumOf { it.shares }

                        if (sharesOnExDate <= 0.0) return@mapNotNull null

                        DividendPayment(
                            id = DividendPaymentId("$ticker-${event.exDividendDate}"),
                            tickerId = ticker,
                            amount = event.amountPerShare * sharesOnExDate,
                            amountPerShare = event.amountPerShare,
                            shares = sharesOnExDate,
                            currency = event.currency,
                            paymentDate = event.exDividendDate,
                        )
                    }
                }
            }.awaitAll().flatten()
        }

        // Replace the entire cache — removes any stale pre-purchase dividends
        dividendRepository.replaceAllPayments(eligiblePayments)
    }
}
