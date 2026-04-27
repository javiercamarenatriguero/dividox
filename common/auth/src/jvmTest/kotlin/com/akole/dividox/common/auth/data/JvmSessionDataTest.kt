package com.akole.dividox.common.auth.data

import com.akole.dividox.common.auth.domain.model.AuthProvider
import com.akole.dividox.common.auth.domain.model.AuthUser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class JvmSessionDataTest {

    // region toAuthUser

    @Test
    fun `to auth user maps all non-token fields`() {
        // GIVEN
        val session = JvmSessionData(
            uid = "uid-001",
            email = "user@example.com",
            displayName = "Jane Doe",
            provider = AuthProvider.EMAIL,
            idToken = "secret_id_token",
            refreshToken = "secret_refresh_token",
        )

        // WHEN
        val user = session.toAuthUser()

        // THEN
        assertEquals("uid-001", user.uid)
        assertEquals("user@example.com", user.email)
        assertEquals("Jane Doe", user.displayName)
        assertEquals(AuthProvider.EMAIL, user.provider)
    }

    @Test
    fun `to auth user preserves null email and display name`() {
        // GIVEN
        val session = JvmSessionData(
            uid = "uid-002",
            email = null,
            displayName = null,
            provider = AuthProvider.GOOGLE,
            idToken = "token",
            refreshToken = "refresh",
        )

        // WHEN
        val user = session.toAuthUser()

        // THEN
        assertNull(user.email)
        assertNull(user.displayName)
    }

    // endregion

    // region fromAuthUser

    @Test
    fun `from auth user copies all domain fields and attaches tokens`() {
        // GIVEN
        val user = AuthUser(
            uid = "uid-003",
            email = "user@example.com",
            displayName = "John",
            provider = AuthProvider.EMAIL,
        )
        val idToken = "new_id_token"
        val refreshToken = "new_refresh_token"

        // WHEN
        val session = JvmSessionData.fromAuthUser(user, idToken, refreshToken)

        // THEN
        assertEquals(user.uid, session.uid)
        assertEquals(user.email, session.email)
        assertEquals(user.displayName, session.displayName)
        assertEquals(user.provider, session.provider)
        assertEquals(idToken, session.idToken)
        assertEquals(refreshToken, session.refreshToken)
    }

    @Test
    fun `from auth user followed by to auth user reconstructs original user`() {
        // GIVEN
        val original = AuthUser(
            uid = "uid-004",
            email = "roundtrip@example.com",
            displayName = "Round Trip",
            provider = AuthProvider.EMAIL,
        )

        // WHEN
        val session = JvmSessionData.fromAuthUser(original, "token", "refresh")
        val reconstructed = session.toAuthUser()

        // THEN
        assertEquals(original, reconstructed)
    }

    @Test
    fun `from auth user with null email and display name`() {
        // GIVEN
        val user = AuthUser(
            uid = "uid-005",
            email = null,
            displayName = null,
            provider = AuthProvider.GOOGLE,
        )

        // WHEN
        val session = JvmSessionData.fromAuthUser(user, "token", "refresh")

        // THEN
        assertNull(session.email)
        assertNull(session.displayName)
    }

    // endregion
}
