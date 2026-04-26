package com.akole.dividox.common.auth.domain.usecase

import com.akole.dividox.common.auth.domain.repository.AuthRepository

/**
 * Use case for Google Sign-In authentication.
 */
class SignInWithGoogleUseCase(private val repository: AuthRepository) {
    /**
     * Executes Google sign-in with ID token.
     * @param idToken Google ID token from sign-in flow
     * @return Success with Unit or failure
     */
    suspend operator fun invoke(idToken: String): Result<Unit> =
        repository.signInWithGoogle(idToken)
}
