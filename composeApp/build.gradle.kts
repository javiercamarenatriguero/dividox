import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.dividox.kmp.application)
    alias(libs.plugins.dividox.compose.multiplatform)
    alias(libs.plugins.dividox.kmp.ios)
    alias(libs.plugins.dividox.kmp.test)
    alias(libs.plugins.dividox.detekt)
    alias(libs.plugins.kotlinxSerialization)
}

android {
    namespace = "com.akole.dividox"
    defaultConfig {
        applicationId = "com.akole.dividox"
        versionCode = 1
        versionName = "1.0"
    }
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.common.mvi)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.core)
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
