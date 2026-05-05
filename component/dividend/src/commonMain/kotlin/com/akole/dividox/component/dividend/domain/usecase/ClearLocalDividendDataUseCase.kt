package com.akole.dividox.component.dividend.domain.usecase

import com.akole.dividox.component.dividend.domain.repository.DividendRepository

/**
 * Clears all locally cached dividend payments.
 *
 * Must be called on sign-out, before or alongside [SignOutUseCase], to ensure
 * no stale dividend data leaks into the next user session.
 */
class ClearLocalDividendDataUseCase(private val repository: DividendRepository) {
    suspend operator fun invoke() = repository.clearAll()
}
