package com.akole.dividox

import androidx.compose.ui.window.ComposeUIViewController
import com.akole.dividox.di.KoinInitializer

fun MainViewController() = run {
    initGoogleSignIn()
    KoinInitializer.init()
    ComposeUIViewController { App() }
}
