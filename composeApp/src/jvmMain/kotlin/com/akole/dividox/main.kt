package com.akole.dividox

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.akole.dividox.di.KoinInitializer
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.ic_dividox
import org.jetbrains.compose.resources.painterResource

fun main() {
    KoinInitializer.init()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "DiviDox",
            icon = painterResource(Res.drawable.ic_dividox),
        ) {
            App()
        }
    }
}
