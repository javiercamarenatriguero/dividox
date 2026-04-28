package com.akole.dividox.component.auth.domain.usecase

import com.akole.dividox.component.auth.domain.repository.AuthRepository

/**
 * Use case for signing out current user and clearing session.
 */
class SignOutUseCase(private val repository: AuthRepository) {
    /**
     * Signs out current user.
     * @return Success with Unit or failure
     */
    suspend operator fun invoke(): Result<Unit> =
        repository.signOut()
}
