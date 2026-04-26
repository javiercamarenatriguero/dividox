package com.akole.dividox.common.auth.domain.usecase

import com.akole.dividox.common.auth.domain.FailingAuthRepository
import com.akole.dividox.common.auth.domain.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ForgotPasswordUseCaseTest {

    @Test
    fun invokesRepositorySendPasswordResetEmailWithEmail() = runTest {
        // GIVEN
        val repository = FakeAuthRepository()
        val useCase = ForgotPasswordUseCase(repository)
        val email = "user@example.com"

        // WHEN
        val result = useCase(email)

        // THEN
        assertTrue(result.isSuccess)
        assertTrue(repository.sendPasswordResetEmailCalled)
    }

    @Test
    fun returnsFailureWhenRepositoryThrowsException() = runTest {
        // GIVEN
        val repository = FailingAuthRepository()
        val useCase = ForgotPasswordUseCase(repository)

        // WHEN
        val result = useCase("nonexistent@example.com")

        // THEN
        assertTrue(result.isFailure)
    }
}
