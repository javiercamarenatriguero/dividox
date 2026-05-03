package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import kotlinx.coroutines.flow.Flow

/**
 * Returns the cumulative sum of all CASH dividend payments.
 * REINVESTED payments are excluded from the total.
 *
 * @property repository Source of dividend data.
 */
class GetLifetimeDividendsUseCase(private val repository: DividendRepository) {
    operator fun invoke(): Flow<Double> = repository.getLifetimeDividends()
}
