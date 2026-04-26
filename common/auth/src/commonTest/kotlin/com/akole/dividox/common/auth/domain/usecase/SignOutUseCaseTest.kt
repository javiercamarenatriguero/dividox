package com.akole.dividox.common.auth.domain.usecase

import com.akole.dividox.common.auth.domain.FailingAuthRepository
import com.akole.dividox.common.auth.domain.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SignOutUseCaseTest {

    @Test
    fun invokesRepositorySignOut() = runTest {
        // GIVEN
        val repository = FakeAuthRepository()
        val useCase = SignOutUseCase(repository)

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isSuccess)
        assertTrue(repository.signOutCalled)
    }

    @Test
    fun returnsFailureWhenRepositoryThrowsException() = runTest {
        // GIVEN
        val repository = FailingAuthRepository()
        val useCase = SignOutUseCase(repository)

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isFailure)
    }
}
