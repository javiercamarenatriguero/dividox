@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.akole.dividox

import cocoapods.GoogleSignIn.GIDSignIn
import com.akole.dividox.component.auth.data.GoogleSignInLauncher
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun initGoogleSignIn() {
    GoogleSignInLauncher.signInProvider = {
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
            ?: throw IllegalStateException("No root view controller available")

        suspendCancellableCoroutine { cont ->
            GIDSignIn.sharedInstance().signInWithPresentingViewController(rootVC) { result, error ->
                when {
                    error != null -> cont.resumeWithException(Exception(error.localizedDescription))
                    result != null -> cont.resume(result.user().idToken()?.tokenString())
                    else -> cont.resume(null)
                }
            }
        }
    }
}
