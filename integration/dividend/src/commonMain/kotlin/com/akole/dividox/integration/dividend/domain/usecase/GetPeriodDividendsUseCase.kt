package com.akole.dividox.integration.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Returns the sum of dividends paid since [from] (inclusive).
 * When [from] is null, returns all-time lifetime dividends.
 */
class GetPeriodDividendsUseCase(private val dividendRepository: DividendRepository) {
    operator fun invoke(from: LocalDate?): Flow<Double> =
        if (from == null) dividendRepository.getLifetimeDividends()
        else dividendRepository.getDividendsSince(from)
}
