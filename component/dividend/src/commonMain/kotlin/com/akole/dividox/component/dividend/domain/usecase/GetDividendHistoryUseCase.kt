package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import kotlinx.coroutines.flow.Flow

/**
 * Returns the full dividend payment history ordered by payment date descending.
 *
 * Delegates to [DividendRepository.getDividendHistory].
 *
 * @property repository Source of dividend data.
 */
class GetDividendHistoryUseCase(private val repository: DividendRepository) {
    operator fun invoke(): Flow<List<DividendPayment>> = repository.getDividendHistory()
}
