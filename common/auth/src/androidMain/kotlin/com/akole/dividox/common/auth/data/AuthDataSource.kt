package com.akole.dividox.common.auth.data

import com.akole.dividox.common.auth.domain.model.AuthProvider
import com.akole.dividox.common.auth.domain.model.AuthUser
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Android implementation of [AuthDataSource] backed by the Firebase Authentication SDK.
 *
 * Firebase Tasks are bridged to coroutines via [kotlinx.coroutines.tasks.await].
 * Auth-state changes are observed through a [FirebaseAuth.AuthStateListener] wrapped in [callbackFlow].
 */
actual class AuthDataSource actual constructor() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Signs in an existing user with [email] and [password]. */
    actual suspend fun signInWithEmail(email: String, password: String): AuthUser =
        auth.signInWithEmailAndPassword(email, password).await().requireUser()

    /** Creates a new account with [email] and [password]. */
    actual suspend fun signUpWithEmail(email: String, password: String): AuthUser =
        auth.createUserWithEmailAndPassword(email, password).await().requireUser()

    /**
     * Signs in using a Google ID token obtained from Google Sign-In.
     *
     * @param idToken the ID token returned by the Google Sign-In SDK.
     */
    actual suspend fun signInWithGoogle(idToken: String): AuthUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).await().requireUser()
    }

    /** Sends a password-reset e-mail to [email]. */
    actual suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    /** Signs out the currently authenticated user. */
    actual suspend fun signOut() {
        auth.signOut()
    }

    /** Returns UID of currently authenticated user, or null if unauthenticated. */
    actual fun getCurrentUserId(): String? = auth.currentUser?.uid

    /**
     * Emits the current [AuthUser] whenever the Firebase auth state changes.
     * Emits `null` when the user is signed out.
     * The listener is automatically removed when the collector is cancelled.
     */
    actual fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toAuthUser()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}

private fun AuthResult.requireUser(): AuthUser =
    user?.toAuthUser() ?: error("Firebase returned no user after successful sign-in")

private fun FirebaseUser.toAuthUser() = AuthUser(
    uid = uid,
    email = email,
    displayName = displayName,
    provider = providerData.firstOrNull()?.providerId?.toAuthProvider() ?: AuthProvider.EMAIL,
)

private fun String.toAuthProvider() = when (this) {
    GoogleAuthProvider.PROVIDER_ID -> AuthProvider.GOOGLE
    else -> AuthProvider.EMAIL
}
