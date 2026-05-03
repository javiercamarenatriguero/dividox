package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.model.DividendPayment
import com.akole.dividox.component.dividend.domain.repository.DividendRepository

/**
 * Records a new dividend payment.
 *
 * Writes to Firestore; the local Room cache is updated reactively.
 *
 * @property repository Destination for the new payment.
 */
class AddDividendPaymentUseCase(private val repository: DividendRepository) {
    suspend operator fun invoke(payment: DividendPayment) = repository.addDividendPayment(payment)
}
