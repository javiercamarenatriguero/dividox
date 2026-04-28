package com.akole.dividox.common.auth.domain.usecase

import com.akole.dividox.common.auth.domain.repository.AuthRepository

/**
 * Returns the UID of the currently authenticated user.
 * Intended for use at DI wiring time—call after the user has signed in.
 *
 * @throws IllegalStateException if no user is currently authenticated
 */
class GetCurrentUserIdUseCase(private val repository: AuthRepository) {
    operator fun invoke(): String =
        repository.getCurrentUserId()
            ?: throw IllegalStateException("No authenticated user. User must sign in first.")
}
