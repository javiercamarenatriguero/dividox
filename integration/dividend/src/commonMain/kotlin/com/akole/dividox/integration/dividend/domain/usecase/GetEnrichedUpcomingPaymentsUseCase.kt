package com.akole.dividox.integration.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import com.akole.dividox.component.market.domain.repository.MarketRepository
import com.akole.dividox.integration.dividend.domain.model.EnrichedPayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Returns upcoming dividend payments enriched with [com.akole.dividox.component.market.domain.model.CompanyInfo].
 *
 * Company metadata is fetched for each unique ticker in parallel. When the
 * market data source fails for a specific ticker, [EnrichedPayment.companyInfo]
 * is `null` and the payment is still included in the list.
 *
 * The resulting list is sorted by [com.akole.dividox.component.dividend.domain.model.DividendPayment.paymentDate]
 * ascending (soonest first).
 */
class GetEnrichedUpcomingPaymentsUseCase(
    private val dividendRepository: DividendRepository,
    private val marketRepository: MarketRepository,
) {
    operator fun invoke(): Flow<List<EnrichedPayment>> =
        dividendRepository.getUpcomingPayments().map { payments ->
            val companyInfoCache = payments
                .map { it.tickerId }
                .distinct()
                .associateWith { ticker -> marketRepository.getCompanyInfo(ticker).getOrNull() }

            payments
                .sortedBy { it.paymentDate }
                .map { payment ->
                    EnrichedPayment(
                        payment = payment,
                        companyInfo = companyInfoCache[payment.tickerId],
                    )
                }
        }
}
