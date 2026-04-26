package com.akole.dividox.common.auth.domain.usecase

import com.akole.dividox.common.auth.domain.FailingAuthRepository
import com.akole.dividox.common.auth.domain.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SignInWithGoogleUseCaseTest {

    @Test
    fun invokesRepositorySignInWithGoogleWithIdToken() = runTest {
        // GIVEN
        val repository = FakeAuthRepository()
        val useCase = SignInWithGoogleUseCase(repository)
        val idToken = "google_id_token_xyz"

        // WHEN
        val result = useCase(idToken)

        // THEN
        assertTrue(result.isSuccess)
        assertTrue(repository.signInWithGoogleCalled)
    }

    @Test
    fun returnsFailureWhenRepositoryThrowsException() = runTest {
        // GIVEN
        val repository = FailingAuthRepository()
        val useCase = SignInWithGoogleUseCase(repository)

        // WHEN
        val result = useCase("invalid_token")

        // THEN
        assertTrue(result.isFailure)
    }
}
