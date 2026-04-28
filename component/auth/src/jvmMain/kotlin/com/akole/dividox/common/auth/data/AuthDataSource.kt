package com.akole.dividox.component.auth.data

import com.akole.dividox.component.auth.domain.model.AuthProvider
import com.akole.dividox.component.auth.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeoutOrNull
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Desktop (JVM) implementation of [AuthDataSource] using the Firebase Identity Platform REST API.
 *
 * All HTTP calls are made with [java.net.HttpURLConnection] — no third-party HTTP client is used.
 * Successful sessions are persisted to [JvmSessionStorage] so the user remains signed in across
 * app restarts. Auth state is resolved once per cold start (no long-lived listener).
 *
 * REST endpoint mapping:
 * - [signInWithEmail]        → `accounts:signInWithPassword`
 * - [signUpWithEmail]        → `accounts:signUp`
 * - [signInWithGoogle]       → `accounts:signInWithIdp`
 * - [sendPasswordResetEmail] → `accounts:sendOobCode`
 */
actual class AuthDataSource actual constructor() {

    private val storage: JvmSessionStorage = JvmSessionStorage()
    private val authState: MutableStateFlow<JvmSessionData?> = MutableStateFlow(storage.load())

    /** Signs in an existing user with [email] and [password] via the REST sign-in endpoint. */
    actual suspend fun signInWithEmail(email: String, password: String): AuthUser {
        val response = firebasePost(
            endpoint = "accounts:signInWithPassword",
            body = """{"email":"${email.escaped()}","password":"${password.escaped()}","returnSecureToken":true}""",
        )
        return parseSessionAndSave(response, AuthProvider.EMAIL).also { authState.value = it }.toAuthUser()
    }

    /** Creates a new account with [email] and [password] via the REST sign-up endpoint. */
    actual suspend fun signUpWithEmail(email: String, password: String): AuthUser {
        val response = firebasePost(
            endpoint = "accounts:signUp",
            body = """{"email":"${email.escaped()}","password":"${password.escaped()}","returnSecureToken":true}""",
        )
        return parseSessionAndSave(response, AuthProvider.EMAIL).also { authState.value = it }.toAuthUser()
    }

    /**
     * Signs in using a Google ID token via the Identity Platform `signInWithIdp` endpoint.
     *
     * @param idToken the ID token returned by the Google Sign-In flow.
     */
    actual suspend fun signInWithGoogle(idToken: String): AuthUser {
        val postBody = "id_token=${URLEncoder.encode(idToken, "UTF-8")}&providerId=google.com"
        val response = firebasePost(
            endpoint = "accounts:signInWithIdp",
            body = """{"postBody":"$postBody","requestUri":"http://localhost","returnSecureToken":true}""",
        )
        return parseSessionAndSave(response, AuthProvider.GOOGLE).also { authState.value = it }.toAuthUser()
    }

    /** Sends a password-reset e-mail to [email] via the `sendOobCode` endpoint. */
    actual suspend fun sendPasswordResetEmail(email: String) {
        firebasePost(
            endpoint = "accounts:sendOobCode",
            body = """{"requestType":"PASSWORD_RESET","email":"${email.escaped()}"}""",
        )
    }

    /** Clears the persisted session from [JvmSessionStorage], effectively signing out the user. */
    actual suspend fun signOut() {
        storage.clear()
        authState.value = null
    }

    /** Returns UID of currently authenticated user from in-memory session state, or null. */
    actual fun getCurrentUserId(): String? = authState.value?.uid

    /**
     * Emits the current [AuthUser] once based on the persisted session in [JvmSessionStorage].
     *
     * If a session exists and its ID token is expiring soon, a silent refresh is attempted
     * before emitting. Emits `null` when no valid session is found.
     */
    actual fun observeAuthState(): Flow<AuthUser?> = flow {
        val current = authState.value
        if (current != null) {
            val refreshed = refreshIfNeeded(current)
            if (refreshed !== current) authState.value = refreshed
        }
        emitAll(authState.map { it?.toAuthUser() })
    }

    /**
     * Proactively refreshes idToken if expiring within 5 minutes.
     * Timeout 1s to avoid user-visible delay on cold start.
     * Returns refreshed session or null on TOKEN_EXPIRED/USER_DISABLED.
     * Falls back to existing session on network errors (best-effort).
     */
    private suspend fun refreshIfNeeded(session: JvmSessionData): JvmSessionData? {
        if (!JwtDecoder.isExpiringSoon(session.idToken)) return session

        return withTimeoutOrNull(1_000) {
            runCatching { exchangeRefreshToken(session.refreshToken) }
                .fold(
                    onSuccess = { (newIdToken, newRefreshToken) ->
                        session.copy(idToken = newIdToken, refreshToken = newRefreshToken).also { storage.save(it) }
                    },
                    onFailure = { error ->
                        val message = error.message.orEmpty()
                        when {
                            "TOKEN_EXPIRED" in message || "USER_DISABLED" in message -> {
                                storage.clear()
                                null
                            }
                            else -> session // network error: best-effort with existing token
                        }
                    },
                )
        } ?: session // timed out: best-effort
    }

    private fun exchangeRefreshToken(refreshToken: String): Pair<String, String> {
        val url = URL("https://securetoken.googleapis.com/v1/token?key=$FIREBASE_API_KEY")
        val body = "grant_type=refresh_token&refresh_token=${URLEncoder.encode(refreshToken, "UTF-8")}"
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            doOutput = true
            outputStream.use { it.write(body.toByteArray()) }
        }
        return try {
            val response = conn.inputStream.bufferedReader().readText()
            val idToken = extractJsonString(response, "access_token")
            val newRefresh = extractJsonString(response, "refresh_token")
            idToken to newRefresh
        } finally {
            conn.disconnect()
        }
    }

    private fun firebasePost(endpoint: String, body: String): String {
        val url = URL("https://identitytoolkit.googleapis.com/v1/$endpoint?key=$FIREBASE_API_KEY")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            doOutput = true
            outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        }
        return try {
            if (conn.responseCode >= 400) {
                val error = conn.errorStream?.bufferedReader()?.readText().orEmpty()
                val message = runCatching { extractJsonString(error, "message") }.getOrElse { error }
                throw Exception(message)
            }
            conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

    private fun parseSessionAndSave(response: String, provider: AuthProvider): JvmSessionData {
        val session = JvmSessionData(
            uid = extractJsonString(response, "localId"),
            email = runCatching { extractJsonString(response, "email") }.getOrNull(),
            displayName = runCatching { extractJsonString(response, "displayName") }.getOrNull(),
            provider = provider,
            idToken = extractJsonString(response, "idToken"),
            refreshToken = extractJsonString(response, "refreshToken"),
        )
        storage.save(session)
        return session
    }

    private fun extractJsonString(json: String, key: String): String =
        Regex("\"$key\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1)
            ?: error("Missing '$key' in Firebase response")

    private fun String.escaped(): String = replace("\\", "\\\\").replace("\"", "\\\"")

    companion object {
        private const val FIREBASE_API_KEY = "AIzaSyD3rihf42yOStFmc9QYuRJduObM9kTdZ18"
    }
}
