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
import java.util.Properties
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
    val props = Properties()
    Thread.currentThread().contextClassLoader
        ?.getResourceAsStream("firebase.properties")
        ?.use(props::load)
        ?: error("firebase.properties not found in classpath — run ./gradlew :composeApp:jvmProcessResources first")

    fun require(key: String): String =
        props.getProperty(key)?.takeIf { it.isNotEmpty() }
            ?: error("$key missing or empty in firebase.properties")

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
            applicationId = require("applicationId"),
            apiKey = require("apiKey"),
            projectId = require("projectId"),
            gcmSenderId = props.getProperty("gcmSenderId"),
            storageBucket = props.getProperty("storageBucket"),
        ),
    )
}
