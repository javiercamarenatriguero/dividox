package com.akole.dividox.integration.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Returns the full dividend payment history enriched with company metadata.
 *
 * Company info is resolved per unique ticker and cached for the duration of the
 * mapping operation. Payments are ordered by date descending (most recent first).
 * When market data is unavailable for a ticker, [EnrichedPayment.companyInfo] is
 * `null` and the payment is still included.
 */
class GetEnrichedPaymentHistoryUseCase(
    private val dividendRepository: DividendRepository,
    private val marketRepository: MarketRepository,
) {
    operator fun invoke(): Flow<List<EnrichedPayment>> =
        dividendRepository.getDividendHistory().map { payments ->
            val companyInfoCache = payments
                .map { it.tickerId }
                .distinct()
                .associateWith { ticker -> marketRepository.getCompanyInfo(ticker).getOrNull() }

            payments
                .sortedByDescending { it.paymentDate }
                .map { payment ->
                    EnrichedPayment(
                        payment = payment,
                        companyInfo = companyInfoCache[payment.tickerId],
                    )
                }
        }
}
