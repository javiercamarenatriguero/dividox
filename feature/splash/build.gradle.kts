plugins {
    alias(libs.plugins.dividox.kmp.library)
    alias(libs.plugins.dividox.compose.multiplatform)
    alias(libs.plugins.dividox.kmp.ios)
    alias(libs.plugins.dividox.kmp.test)
    alias(libs.plugins.dividox.detekt)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:ui-resources"))
            implementation(libs.compose.foundation)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
