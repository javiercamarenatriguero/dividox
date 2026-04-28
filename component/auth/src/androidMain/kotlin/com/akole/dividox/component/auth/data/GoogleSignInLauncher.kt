package com.akole.dividox.component.auth.data

actual class GoogleSignInLauncher {

    actual suspend fun launchSignIn(): String? = signInProvider?.invoke()

    companion object {
        var signInProvider: (suspend () -> String?)? = null
    }
}
