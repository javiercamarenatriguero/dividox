package com.akole.dividox.component.auth.data

import java.util.Base64

/**
 * Lightweight JWT decoder for extracting exp claim without external library.
 * Uses standard Java Base64 decoder.
 */
object JwtDecoder {
    /**
     * Extracts expiration timestamp (exp) from JWT.
     * @param token JWT token
     * @return Expiration time in seconds since epoch, or Long.MAX_VALUE if invalid
     */
    fun getExpirationTime(token: String): Long = try {
        val parts = token.split(".")
        if (parts.size != 3) return Long.MAX_VALUE

        val decoder = Base64.getUrlDecoder()
        val payload = decoder.decode(parts[1])
        val json = String(payload, Charsets.UTF_8)

        val expMatch = Regex("\"exp\"\\s*:\\s*(\\d+)").find(json)
        expMatch?.groupValues?.get(1)?.toLongOrNull() ?: Long.MAX_VALUE
    } catch (e: Exception) {
        Long.MAX_VALUE
    }

    /**
     * Checks if token expires within [bufferSeconds].
     * @param token JWT token
     * @param bufferSeconds Grace period in seconds (default 300s = 5 min)
     * @return true if token expiring soon
     */
    fun isExpiringSoon(token: String, bufferSeconds: Long = 300): Boolean {
        val exp = getExpirationTime(token)
        val now = System.currentTimeMillis() / 1000
        return (exp - now) <= bufferSeconds
    }
}
