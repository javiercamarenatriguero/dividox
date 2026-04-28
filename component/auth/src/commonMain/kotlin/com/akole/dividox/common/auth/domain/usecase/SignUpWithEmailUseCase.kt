package com.akole.dividox.component.auth.domain.usecase

import com.akole.dividox.component.auth.domain.repository.AuthRepository

/**
 * Use case for creating new user with email and password.
 */
class SignUpWithEmailUseCase(private val repository: AuthRepository) {
    /**
     * Executes user registration.
     * @param email User email
     * @param password User password
     * @return Success with Unit or failure
     */
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        repository.signUpWithEmail(email, password)
}
