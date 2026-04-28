package com.akole.dividox

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.akole.dividox.di.KoinInitializer
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.ic_dividox
import org.jetbrains.compose.resources.painterResource

fun main() {
    Firebase.initialize(
        options = FirebaseOptions(
            apiKey = "AIzaSyD3rihf42yOStFmc9QYuRJduObM9kTdZ18",
            projectId = "dividox-aca23",
            applicationId = "1:836293215181:android:6641e79fa99571cf75ba84",
            gcmSenderId = "836293215181",
        ),
    )
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
