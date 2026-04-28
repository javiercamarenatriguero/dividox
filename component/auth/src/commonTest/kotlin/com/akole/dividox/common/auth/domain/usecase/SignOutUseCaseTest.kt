package com.akole.dividox.component.auth.domain.usecase

import com.akole.dividox.component.auth.domain.FailingAuthRepository
import com.akole.dividox.component.auth.domain.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SignOutUseCaseTest {

    @Test
    fun `invokes repository sign out`() = runTest {
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
    fun `returns failure when repository throws exception`() = runTest {
        // GIVEN
        val repository = FailingAuthRepository()
        val useCase = SignOutUseCase(repository)

        // WHEN
        val result = useCase()

        // THEN
        assertTrue(result.isFailure)
    }
}
