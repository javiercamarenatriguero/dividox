package com.akole.dividox

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.akole.dividox.di.KoinInitializer

fun main() {
    KoinInitializer.init()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "DiviDox",
        ) {
            App()
        }
    }
}
