package com.akole.dividox.component.auth.data

import com.akole.dividox.component.auth.domain.model.AuthProvider
import com.akole.dividox.component.auth.domain.model.AuthUser
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

actual class AuthDataSource actual constructor() {

    actual suspend fun signInWithEmail(email: String, password: String): AuthUser =
        Firebase.auth.signInWithEmailAndPassword(email, password).user!!.toAuthUser()

    actual suspend fun signUpWithEmail(email: String, password: String): AuthUser =
        Firebase.auth.createUserWithEmailAndPassword(email, password).user!!.toAuthUser()

    actual suspend fun signInWithGoogle(idToken: String): AuthUser =
        throw UnsupportedOperationException("Google Sign-In not supported on Desktop")

    actual suspend fun sendPasswordResetEmail(email: String) {
        Firebase.auth.sendPasswordResetEmail(email)
    }

    actual suspend fun signOut() {
        Firebase.auth.signOut()
    }

    actual fun observeAuthState(): Flow<AuthUser?> =
        Firebase.auth.authStateChanged.map { it?.toAuthUser() }

    actual fun getCurrentUserId(): String? = Firebase.auth.currentUser?.uid
}

private fun FirebaseUser.toAuthUser(): AuthUser = AuthUser(
    uid = uid,
    email = email,
    displayName = displayName,
    provider = AuthProvider.EMAIL,
)
