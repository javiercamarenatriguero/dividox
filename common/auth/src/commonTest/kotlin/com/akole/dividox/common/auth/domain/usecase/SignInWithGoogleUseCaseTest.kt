package com.akole.dividox.common.auth.domain.usecase

import com.akole.dividox.common.auth.domain.FailingAuthRepository
import com.akole.dividox.common.auth.domain.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SignInWithGoogleUseCaseTest {

    @Test
    fun `invokes repository with correct id token`() = runTest {
        // GIVEN
        val repository = FakeAuthRepository()
        val useCase = SignInWithGoogleUseCase(repository)

        // WHEN
        val result = useCase("google_id_token_xyz")

        // THEN
        assertTrue(result.isSuccess)
        assertTrue(repository.signInWithGoogleCalled)
    }

    @Test
    fun `returns failure when repository throws exception`() = runTest {
        // GIVEN
        val repository = FailingAuthRepository()
        val useCase = SignInWithGoogleUseCase(repository)

        // WHEN
        val result = useCase("invalid_token")

        // THEN
        assertTrue(result.isFailure)
    }
}
