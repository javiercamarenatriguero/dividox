package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import kotlinx.coroutines.flow.Flow

/**
 * Returns the year-to-date sum of CASH dividend payments.
 *
 * @property repository Source of dividend data.
 */
class GetYtdDividendsUseCase(private val repository: DividendRepository) {
    operator fun invoke(): Flow<Double> = repository.getYtdDividends()
}
