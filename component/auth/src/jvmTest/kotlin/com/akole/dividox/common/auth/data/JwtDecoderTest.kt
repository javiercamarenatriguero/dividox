package com.akole.dividox.component.auth.data

import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JwtDecoderTest {

    // region getExpirationTime

    @Test
    fun `returns exp claim from valid JWT`() {
        // GIVEN
        val token = buildToken(payload = """{"sub":"uid","exp":9999999999}""")

        // WHEN
        val expTime = JwtDecoder.getExpirationTime(token)

        // THEN
        assertEquals(9999999999L, expTime)
    }

    @Test
    fun `returns MAX_LONG when token has only two parts`() {
        // GIVEN
        val token = "header.payload"

        // WHEN
        val expTime = JwtDecoder.getExpirationTime(token)

        // THEN
        assertEquals(Long.MAX_VALUE, expTime)
    }

    @Test
    fun `returns MAX_LONG when token is not JWT format`() {
        // GIVEN
        val token = "not_a_jwt"

        // WHEN
        val expTime = JwtDecoder.getExpirationTime(token)

        // THEN
        assertEquals(Long.MAX_VALUE, expTime)
    }

    @Test
    fun `returns MAX_LONG when payload is missing exp field`() {
        // GIVEN
        val token = buildToken(payload = """{"sub":"uid","iat":1234567890}""")

        // WHEN
        val expTime = JwtDecoder.getExpirationTime(token)

        // THEN
        assertEquals(Long.MAX_VALUE, expTime)
    }

    @Test
    fun `returns MAX_LONG when payload is not valid JSON`() {
        // GIVEN
        val token = "${base64Url("""{"alg":"HS256"}""")}.${base64Url("not-json")}.sig"

        // WHEN
        val expTime = JwtDecoder.getExpirationTime(token)

        // THEN
        assertEquals(Long.MAX_VALUE, expTime)
    }

    // endregion

    // region isExpiringSoon

    @Test
    fun `returns true when token expires within buffer window`() {
        // GIVEN
        val now = System.currentTimeMillis() / 1000
        val token = buildToken(payload = """{"exp":${now + 100}}""")

        // WHEN
        val result = JwtDecoder.isExpiringSoon(token, bufferSeconds = 200)

        // THEN
        assertTrue(result)
    }

    @Test
    fun `returns false when token has plenty of time remaining`() {
        // GIVEN
        val now = System.currentTimeMillis() / 1000
        val token = buildToken(payload = """{"exp":${now + 600}}""")

        // WHEN
        val result = JwtDecoder.isExpiringSoon(token, bufferSeconds = 300)

        // THEN
        assertFalse(result)
    }

    @Test
    fun `returns true when token is already expired`() {
        // GIVEN
        val now = System.currentTimeMillis() / 1000
        val token = buildToken(payload = """{"exp":${now - 60}}""")

        // WHEN
        val result = JwtDecoder.isExpiringSoon(token)

        // THEN
        assertTrue(result)
    }

    @Test
    fun `uses five minute default buffer`() {
        // GIVEN — token expires in 299s, which is less than the 300s default buffer
        val now = System.currentTimeMillis() / 1000
        val token = buildToken(payload = """{"exp":${now + 299}}""")

        // WHEN
        val result = JwtDecoder.isExpiringSoon(token)

        // THEN
        assertTrue(result)
    }

    @Test
    fun `returns false for invalid token because MAX_LONG is far in the future`() {
        // GIVEN — invalid token returns MAX_LONG as exp, MAX_LONG - now >> any buffer
        val token = "invalid"

        // WHEN
        val result = JwtDecoder.isExpiringSoon(token)

        // THEN
        assertFalse(result)
    }

    // endregion

    // region helpers

    private fun buildToken(payload: String): String {
        val header = base64Url("""{"alg":"HS256"}""")
        val encodedPayload = base64Url(payload)
        val sig = base64Url("signature")
        return "$header.$encodedPayload.$sig"
    }

    private fun base64Url(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray())

    // endregion
}
