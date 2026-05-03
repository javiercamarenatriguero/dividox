package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.repository.DividendRepository
import kotlinx.coroutines.flow.Flow

/**
 * Returns scheduled dividend payments with a future payment date.
 *
 * @property repository Source of dividend data.
 */
class GetUpcomingPaymentsUseCase(private val repository: DividendRepository) {
    operator fun invoke(): Flow<List<DividendPayment>> = repository.getUpcomingPayments()
}
