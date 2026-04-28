package com.akole.dividox.component.auth.domain.usecase

import com.akole.dividox.component.auth.domain.repository.AuthRepository

/**
 * Use case for signing in user with email and password.
 */
class SignInWithEmailUseCase(private val repository: AuthRepository) {
    /**
     * Executes email sign-in.
     * @param email User email
     * @param password User password
     * @return Success with Unit or failure
     */
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        repository.signInWithEmail(email, password)
}
