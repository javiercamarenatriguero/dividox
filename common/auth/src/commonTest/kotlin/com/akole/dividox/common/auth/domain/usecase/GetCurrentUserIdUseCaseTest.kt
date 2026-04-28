package com.akole.dividox.common.auth.domain.usecase

import com.akole.dividox.common.auth.domain.FakeAuthRepository
import com.akole.dividox.common.auth.domain.FailingAuthRepository
import com.akole.dividox.common.auth.domain.model.AuthProvider
import com.akole.dividox.common.auth.domain.model.AuthUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetCurrentUserIdUseCaseTest {

    @Test
    fun `returns uid when user is authenticated`() {
        // GIVEN
        val user = AuthUser(
            uid = "user-123",
            email = "test@test.com",
            displayName = null,
            provider = AuthProvider.EMAIL,
        )
        val repository = FakeAuthRepository(user = user)
        val useCase = GetCurrentUserIdUseCase(repository)

        // WHEN
        val result = useCase()

        // THEN
        assertEquals("user-123", result)
    }

    @Test
    fun `throws when no user is authenticated`() {
        // GIVEN
        val repository = FakeAuthRepository(user = null)
        val useCase = GetCurrentUserIdUseCase(repository)

        // WHEN / THEN
        assertFailsWith<IllegalStateException> { useCase() }
    }

    @Test
    fun `throws when repository returns null uid`() {
        // GIVEN
        val repository = FailingAuthRepository()
        val useCase = GetCurrentUserIdUseCase(repository)

        // WHEN / THEN
        assertFailsWith<IllegalStateException> { useCase() }
    }
}
