import com.android.build.api.dsl.ApplicationExtension
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
            implementation(projects.common.auth)
            implementation(projects.common.mvi)
            implementation(projects.common.uiResources)
            implementation(projects.component.portfolio)
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
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
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
