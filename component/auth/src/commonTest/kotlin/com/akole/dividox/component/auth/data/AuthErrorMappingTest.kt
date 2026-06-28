package com.akole.dividox.component.auth.data

import com.akole.dividox.component.auth.domain.exception.RecentLoginRequiredException
import com.akole.dividox.component.auth.domain.model.AuthError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthErrorMappingTest {

    @Test
    fun `invalid password maps to InvalidCredentials`() {
        // GIVEN
        val exception = Exception("The password is invalid or the user does not have a password")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.InvalidCredentials, result)
    }

    @Test
    fun `invalid credential maps to InvalidCredentials`() {
        // GIVEN
        val exception = Exception("The supplied auth credential is incorrect, malformed or has expired")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.InvalidCredentials, result)
    }

    @Test
    fun `wrong password maps to InvalidCredentials`() {
        // GIVEN
        val exception = Exception("WRONG_PASSWORD")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.InvalidCredentials, result)
    }

    @Test
    fun `invalid login credentials maps to InvalidCredentials`() {
        // GIVEN
        val exception = Exception("Invalid login credentials")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.InvalidCredentials, result)
    }

    @Test
    fun `no user record maps to AccountNotFound`() {
        // GIVEN
        val exception = Exception("There is no user record corresponding to this identifier")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.AccountNotFound, result)
    }

    @Test
    fun `user not found maps to AccountNotFound`() {
        // GIVEN
        val exception = Exception("User not found")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.AccountNotFound, result)
    }

    @Test
    fun `email already in use maps to EmailAlreadyInUse`() {
        // GIVEN
        val exception = Exception("The email address is already in use by another account")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.EmailAlreadyInUse, result)
    }

    @Test
    fun `weak password maps to WeakPassword`() {
        // GIVEN
        val exception = Exception("The given password is invalid. Password should be at least 6 characters")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.WeakPassword, result)
    }

    @Test
    fun `badly formatted email maps to InvalidEmail`() {
        // GIVEN
        val exception = Exception("The email address is badly formatted")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.InvalidEmail, result)
    }

    @Test
    fun `network error maps to NetworkError`() {
        // GIVEN
        val exception = Exception("A network error (such as timeout) has occurred")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.NetworkError, result)
    }

    @Test
    fun `too many requests maps to TooManyAttempts`() {
        // GIVEN
        val exception = Exception("We have blocked all requests from this device due to unusual activity")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.TooManyAttempts, result)
    }

    @Test
    fun `too many attempts maps to TooManyAttempts`() {
        // GIVEN
        val exception = Exception("Too many unsuccessful login attempts")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.TooManyAttempts, result)
    }

    @Test
    fun `recent login required maps to RecentLoginRequired`() {
        // GIVEN
        val exception = Exception("This operation requires recent login")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.RecentLoginRequired, result)
    }

    @Test
    fun `RecentLoginRequiredException maps to RecentLoginRequired`() {
        // GIVEN
        val exception = RecentLoginRequiredException()

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertEquals(AuthError.RecentLoginRequired, result)
    }

    @Test
    fun `unknown error maps to Unknown with original message`() {
        // GIVEN
        val exception = Exception("Something completely unexpected happened")

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertIs<AuthError.Unknown>(result)
        assertEquals("Something completely unexpected happened", result.message)
    }

    @Test
    fun `null message maps to Unknown with default message`() {
        // GIVEN
        val exception = Exception(null as String?)

        // WHEN
        val result = exception.toAuthError()

        // THEN
        assertIs<AuthError.Unknown>(result)
        assertEquals("Unknown authentication error", result.message)
    }
}
