package com.akole.dividox

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.akole.dividox.component.auth.data.GoogleSignInLauncher
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

private const val WEB_CLIENT_ID =
    "836293215181-ba3sl7lmkbu08ogl5esrvmp0ks4cuagk.apps.googleusercontent.com"

fun initGoogleSignIn(context: Context) {
    GoogleSignInLauncher.signInProvider = {
        val credentialManager = CredentialManager.create(context)
        getGoogleIdToken(credentialManager, context)
    }
}

private suspend fun getGoogleIdToken(
    credentialManager: CredentialManager,
    context: Context,
): String? {
    return try {
        requestGoogleIdToken(credentialManager, context, filterByAuthorized = false)
    } catch (_: NoCredentialException) {
        // Fall back to the full Sign-In with Google bottom sheet
        try {
            requestSignInWithGoogle(credentialManager, context)
        } catch (_: GetCredentialCancellationException) {
            null
        }
    } catch (_: GetCredentialCancellationException) {
        null
    }
}

private suspend fun requestGoogleIdToken(
    credentialManager: CredentialManager,
    context: Context,
    filterByAuthorized: Boolean,
): String? {
    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(filterByAuthorized)
        .setServerClientId(WEB_CLIENT_ID)
        .setAutoSelectEnabled(false)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()

    val response = credentialManager.getCredential(context, request)
    return response.credential.toGoogleIdToken()
}

private suspend fun requestSignInWithGoogle(
    credentialManager: CredentialManager,
    context: Context,
): String? {
    val option = GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID).build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()

    val response = credentialManager.getCredential(context, request)
    return response.credential.toGoogleIdToken()
}

private fun androidx.credentials.Credential.toGoogleIdToken(): String? =
    if (this is CustomCredential &&
        type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        GoogleIdTokenCredential.createFrom(data).idToken
    } else {
        null
    }
