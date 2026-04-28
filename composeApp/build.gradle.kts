import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.dividox.kmp.application)
    alias(libs.plugins.dividox.compose.multiplatform)
    alias(libs.plugins.dividox.kmp.ios)
    alias(libs.plugins.dividox.kmp.di)
    alias(libs.plugins.dividox.kmp.test)
    alias(libs.plugins.dividox.detekt)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.dividox.kmp.cocoapods)
}

extensions.configure<ApplicationExtension> {
    namespace = "com.akole.dividox"
    defaultConfig {
        applicationId = "com.akole.dividox"
        versionCode = (project.findProperty("buildNumber") as String?)?.toIntOrNull() ?: 1
        versionName = "1.0"
    }
}

kotlin {
    jvm()

    cocoapods {
        summary = "Dividox KMP app"
        homepage = "https://dividox.app"
        version = "1.0"
        // Min version
        ios.deploymentTarget = "14.0"
        framework {
            baseName = "ComposeApp"
            isStatic = true
        }
        pod("FirebaseAuth") { version = "~> 11.0" }
        pod("FirebaseFirestore") { version = "~> 11.0" }
        pod("GoogleSignIn") { version = "~> 8.0" }
        podfile = project.file("../iosApp/Podfile")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.component.auth)
            implementation(projects.common.network)
            implementation(projects.common.mvi)
            implementation(projects.common.uiResources)
            implementation(projects.component.portfolio)
            implementation(projects.component.market)
            implementation(projects.feature.auth)
            implementation(projects.feature.details)
            implementation(projects.feature.home)
            implementation(projects.feature.splash)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.core)
        }
        androidMain.dependencies {
            implementation(libs.androidx.credentials)
            implementation(libs.androidx.credentials.play.services.auth)
            implementation(libs.googleid)
        }
        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
                implementation(libs.firebase.kotlin.auth)
            }
            resources.srcDir(layout.buildDirectory.dir("generated/firebase"))
        }
    }
}

val generateFirebaseConfig by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/firebase")
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) inputs.file(localPropsFile)
    inputs.property("applicationId", System.getenv("FIREBASE_APPLICATION_ID") ?: "")
    inputs.property("apiKey", System.getenv("FIREBASE_API_KEY") ?: "")
    inputs.property("projectId", System.getenv("FIREBASE_PROJECT_ID") ?: "")
    inputs.property("gcmSenderId", System.getenv("FIREBASE_GCM_SENDER_ID") ?: "")
    inputs.property("storageBucket", System.getenv("FIREBASE_STORAGE_BUCKET") ?: "")
    outputs.dir(outputDir)

    doLast {
        val localProps = Properties()
        if (localPropsFile.exists()) localPropsFile.inputStream().use(localProps::load)

        fun resolve(propKey: String, envKey: String): String =
            System.getenv(envKey)?.takeIf { it.isNotEmpty() }
                ?: localProps.getProperty(propKey, "")

        outputDir.get().asFile.also { it.mkdirs() }
            .resolve("firebase.properties")
            .writeText(
                buildString {
                    appendLine("applicationId=${resolve("firebase.desktop.applicationId", "FIREBASE_APPLICATION_ID")}")
                    appendLine("apiKey=${resolve("firebase.desktop.apiKey", "FIREBASE_API_KEY")}")
                    appendLine("projectId=${resolve("firebase.desktop.projectId", "FIREBASE_PROJECT_ID")}")
                    appendLine("gcmSenderId=${resolve("firebase.desktop.gcmSenderId", "FIREBASE_GCM_SENDER_ID")}")
                    append("storageBucket=${resolve("firebase.desktop.storageBucket", "FIREBASE_STORAGE_BUCKET")}")
                },
            )
    }
}

tasks.named("jvmProcessResources") {
    dependsOn(generateFirebaseConfig)
}

compose.desktop {
    application {
        mainClass = "com.akole.dividox.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.akole.dividox"
            packageVersion = "1.0.0"
        }
    }
}
