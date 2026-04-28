package com.akole.dividox.component.auth.domain.usecase

import com.akole.dividox.component.auth.domain.repository.AuthRepository

/**
 * Use case for password reset flow. Sends password reset email to user.
 */
class ForgotPasswordUseCase(private val repository: AuthRepository) {
    /**
     * Sends password reset email.
     * @param email User email address
     * @return Success with Unit or failure
     */
    suspend operator fun invoke(email: String): Result<Unit> =
        repository.sendPasswordResetEmail(email)
}
