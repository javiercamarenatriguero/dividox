@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.akole.dividox.common.auth.data

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRGoogleAuthProvider
import cocoapods.FirebaseAuth.FIRUser
import cocoapods.FirebaseAuth.FIRUserInfoProtocol
import com.akole.dividox.common.auth.domain.model.AuthProvider
import com.akole.dividox.common.auth.domain.model.AuthUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * iOS implementation of [AuthDataSource] backed by the Firebase Authentication SDK via
 * Kotlin/Native CocoaPods interop (`cocoapods.FirebaseAuth`).
 *
 * ObjC callback-based APIs are bridged to coroutines with [suspendCancellableCoroutine].
 * Note: K/N interop exposes ObjC properties as functions (`uid()`, `email()`, `user()`, etc.).
 */
actual class AuthDataSource actual constructor() {

    /** Signs in an existing user with [email] and [password]. */
    actual suspend fun signInWithEmail(email: String, password: String): AuthUser =
        suspendCancellableCoroutine { cont ->
            FIRAuth.auth().signInWithEmail(email, password = password) { result, error ->
                cont.resumeWith(result?.user()?.toAuthUser(), error)
            }
        }

    /** Creates a new account with [email] and [password]. */
    actual suspend fun signUpWithEmail(email: String, password: String): AuthUser =
        suspendCancellableCoroutine { cont ->
            FIRAuth.auth().createUserWithEmail(email, password = password) { result, error ->
                cont.resumeWith(result?.user()?.toAuthUser(), error)
            }
        }

    /**
     * Signs in using a Google ID token obtained from Google Sign-In.
     *
     * @param idToken the ID token returned by the Google Sign-In SDK.
     */
    actual suspend fun signInWithGoogle(idToken: String): AuthUser =
        suspendCancellableCoroutine { cont ->
            val credential = FIRGoogleAuthProvider.credentialWithIDToken(idToken, accessToken = "")
            FIRAuth.auth().signInWithCredential(credential) { result, error ->
                cont.resumeWith(result?.user()?.toAuthUser(), error)
            }
        }

    /** Sends a password-reset e-mail to [email]. */
    actual suspend fun sendPasswordResetEmail(email: String): Unit =
        suspendCancellableCoroutine { cont ->
            FIRAuth.auth().sendPasswordResetWithEmail(email) { error ->
                if (error != null) cont.resumeWithException(error.toException())
                else cont.resume(Unit)
            }
        }

    /** Signs out the currently authenticated user. */
    actual suspend fun signOut() {
        FIRAuth.auth().signOut(null)
    }

    /**
     * Emits the current [AuthUser] whenever the Firebase auth state changes.
     * Emits `null` when the user is signed out.
     * The listener handle is removed when the collector is cancelled.
     */
    actual fun observeAuthState(): Flow<AuthUser?> = callbackFlow {
        val handle = FIRAuth.auth().addAuthStateDidChangeListener { _, user ->
            trySend(user?.toAuthUser())
        }
        awaitClose { FIRAuth.auth().removeAuthStateDidChangeListener(handle) }
    }
}

private fun FIRUser.toAuthUser() = AuthUser(
    uid = uid(),
    email = email(),
    displayName = displayName(),
    provider = providerData()
        .filterIsInstance<FIRUserInfoProtocol>()
        .firstOrNull()
        ?.providerID()
        ?.toAuthProvider()
        ?: AuthProvider.EMAIL,
)

private fun String.toAuthProvider() = when (this) {
    "google.com" -> AuthProvider.GOOGLE
    else -> AuthProvider.EMAIL
}

private fun NSError.toException() = Exception(localizedDescription)

private fun <T : Any> CancellableContinuation<T>.resumeWith(value: T?, error: NSError?) {
    when {
        error != null -> resumeWithException(error.toException())
        value != null -> resume(value)
        else -> resumeWithException(Exception("Firebase returned no result and no error"))
    }
}
