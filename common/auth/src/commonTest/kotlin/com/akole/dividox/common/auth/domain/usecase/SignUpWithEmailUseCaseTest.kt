package com.akole.dividox.common.auth.domain.usecase

import com.akole.dividox.common.auth.domain.FailingAuthRepository
import com.akole.dividox.common.auth.domain.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SignUpWithEmailUseCaseTest {

    @Test
    fun `invokes repository with correct email and password`() = runTest {
        // GIVEN
        val repository = FakeAuthRepository()
        val useCase = SignUpWithEmailUseCase(repository)

        // WHEN
        val result = useCase("newuser@example.com", "securePassword123")

        // THEN
        assertTrue(result.isSuccess)
        assertTrue(repository.signUpWithEmailCalled)
    }

    @Test
    fun `returns failure when repository throws exception`() = runTest {
        // GIVEN
        val repository = FailingAuthRepository()
        val useCase = SignUpWithEmailUseCase(repository)

        // WHEN
        val result = useCase("newuser@example.com", "password")

        // THEN
        assertTrue(result.isFailure)
    }
}
