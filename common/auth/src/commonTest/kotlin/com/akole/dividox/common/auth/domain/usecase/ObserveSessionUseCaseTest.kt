package com.akole.dividox.common.auth.domain.usecase

import com.akole.dividox.common.auth.domain.FakeAuthRepository
import com.akole.dividox.common.auth.domain.model.AuthProvider
import com.akole.dividox.common.auth.domain.model.AuthUser
import com.akole.dividox.common.auth.domain.model.SessionState
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ObserveSessionUseCaseTest {

    @Test
    fun `emits Loading then Unauthenticated when no user is signed in`() = runTest {
        // GIVEN
        val useCase = ObserveSessionUseCase(FakeAuthRepository(user = null))

        // WHEN
        val states = useCase().toList()

        // THEN
        assertEquals(2, states.size)
        assertIs<SessionState.Loading>(states[0])
        assertIs<SessionState.Unauthenticated>(states[1])
    }

    @Test
    fun `emits Loading then Authenticated when a user is signed in`() = runTest {
        // GIVEN
        val user = AuthUser(
            uid = "uid-123",
            email = "user@example.com",
            displayName = "Test User",
            provider = AuthProvider.GOOGLE,
        )
        val useCase = ObserveSessionUseCase(FakeAuthRepository(user = user))

        // WHEN
        val states = useCase().toList()

        // THEN
        assertEquals(2, states.size)
        assertIs<SessionState.Loading>(states[0])
        val authenticated = assertIs<SessionState.Authenticated>(states[1])
        assertEquals(user, authenticated.user)
    }

    @Test
    fun `first emission is always Loading`() = runTest {
        // GIVEN
        val useCase = ObserveSessionUseCase(FakeAuthRepository(user = null))

        // WHEN
        val firstState = useCase().toList().first()

        // THEN
        assertIs<SessionState.Loading>(firstState)
    }

}
