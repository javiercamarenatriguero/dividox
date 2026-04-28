plugins {
    alias(libs.plugins.dividox.kmp.library)
    alias(libs.plugins.dividox.compose.multiplatform)
    alias(libs.plugins.dividox.kmp.ios)
    alias(libs.plugins.dividox.kmp.test)
    alias(libs.plugins.dividox.detekt)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":common:auth"))
            implementation(project(":common:mvi"))
            implementation(project(":common:ui-resources"))
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
