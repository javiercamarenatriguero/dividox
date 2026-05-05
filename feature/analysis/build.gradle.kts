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
            implementation(project(":common:mvi"))
            implementation(project(":common:ui-resources"))
            implementation(project(":common:network"))
            implementation(project(":common:settings"))
            implementation(project(":common:currency"))
            implementation(project(":component:market"))
            implementation(project(":component:portfolio"))
            implementation(project(":component:watchlist"))
            implementation(project(":integration:security"))
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlin.test)
        }
    }
}
