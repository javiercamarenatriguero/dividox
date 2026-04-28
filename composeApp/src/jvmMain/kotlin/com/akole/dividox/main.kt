package com.akole.dividox

import android.app.Application
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.akole.dividox.di.KoinInitializer
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import dividox.common.ui_resources.generated.resources.Res
import dividox.common.ui_resources.generated.resources.ic_dividox
import org.jetbrains.compose.resources.painterResource

fun main() {
    initFirebaseJvm()
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

private fun initFirebaseJvm() {
    FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
        private val storage = HashMap<String, String>()
        override fun store(key: String, value: String) { storage[key] = value }
        override fun retrieve(key: String): String? = storage[key]
        override fun clear(key: String) { storage.remove(key) }
        override fun log(msg: String) = println(msg)
    })
    Firebase.initialize(
        context = Application(),
        options = FirebaseOptions(
            applicationId = "1:836293215181:android:6641e79fa99571cf75ba84",
            apiKey = "AIzaSyD3rihf42yOStFmc9QYuRJduObM9kTdZ18",
            projectId = "dividox-aca23",
            gcmSenderId = "836293215181",
            storageBucket = "dividox-aca23.firebasestorage.app",
        ),
    )
}
