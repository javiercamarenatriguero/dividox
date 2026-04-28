package com.akole.dividox.component.auth.domain.usecase

import com.akole.dividox.component.auth.domain.FakeAuthRepository
import com.akole.dividox.component.auth.domain.model.AuthProvider
import com.akole.dividox.component.auth.domain.model.AuthUser
import com.akole.dividox.component.auth.domain.model.SessionState
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveSessionUseCaseTest {

    @Test
    fun `emits Loading then Unauthenticated when no user is present`() = runTest {
        // GIVEN
        val useCase = ObserveSessionUseCase(FakeAuthRepository(user = null))

        // WHEN
        val states = useCase().toList()

        // THEN
        assertEquals(SessionState.Loading, states[0])
        assertEquals(SessionState.Unauthenticated, states[1])
    }

    @Test
    fun `emits Loading then Authenticated when user is present`() = runTest {
        // GIVEN
        val user = AuthUser(
            uid = "uid-123",
            email = "user@example.com",
            displayName = "Test User",
            provider = AuthProvider.EMAIL,
        )
        val useCase = ObserveSessionUseCase(FakeAuthRepository(user = user))

        // WHEN
        val states = useCase().toList()

        // THEN
        assertEquals(SessionState.Loading, states[0])
        val authenticated = assertIs<SessionState.Authenticated>(states[1])
        assertEquals(user, authenticated.user)
    }

    @Test
    fun `authenticated state contains the exact user returned by repository`() = runTest {
        // GIVEN
        val user = AuthUser(
            uid = "uid-456",
            email = null,
            displayName = null,
            provider = AuthProvider.GOOGLE,
        )
        val useCase = ObserveSessionUseCase(FakeAuthRepository(user = user))

        // WHEN
        val states = useCase().toList()

        // THEN
        val authenticated = assertIs<SessionState.Authenticated>(states.last())
        assertEquals("uid-456", authenticated.user.uid)
        assertEquals(AuthProvider.GOOGLE, authenticated.user.provider)
    }
}
