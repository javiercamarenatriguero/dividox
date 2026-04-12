package com.akole.dividox.common.auth.domain.usecase

import com.akole.dividox.common.auth.domain.model.AuthProvider
import com.akole.dividox.common.auth.domain.model.AuthUser
import com.akole.dividox.common.auth.domain.model.SessionState
import com.akole.dividox.common.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveSessionUseCaseTest {

    @Test
    fun `emits Loading then Unauthenticated when no user is signed in`() = runTest {
        val useCase = ObserveSessionUseCase(FakeAuthRepository(user = null))

        val states = useCase().toList()

        assertEquals(2, states.size)
        assertIs<SessionState.Loading>(states[0])
        assertIs<SessionState.Unauthenticated>(states[1])
    }

    @Test
    fun `emits Loading then Authenticated when a user is signed in`() = runTest {
        val user = AuthUser(
            uid = "uid-123",
            email = "user@example.com",
            displayName = "Test User",
            provider = AuthProvider.GOOGLE,
        )
        val useCase = ObserveSessionUseCase(FakeAuthRepository(user = user))

        val states = useCase().toList()

        assertEquals(2, states.size)
        assertIs<SessionState.Loading>(states[0])
        val authenticated = assertIs<SessionState.Authenticated>(states[1])
        assertEquals(user, authenticated.user)
    }

    @Test
    fun `first emission is always Loading`() = runTest {
        val useCase = ObserveSessionUseCase(FakeAuthRepository(user = null))

        val firstState = useCase().toList().first()

        assertIs<SessionState.Loading>(firstState)
    }

    private class FakeAuthRepository(private val user: AuthUser?) : AuthRepository {
        override fun observeAuthState(): Flow<AuthUser?> = flowOf(user)
    }
}
