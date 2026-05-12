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
            api(projects.common.mvi)
            api(projects.common.uiResources)
            implementation(projects.common.settings)
            implementation(projects.component.auth)
            implementation(projects.component.portfolio)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mockk)
        }
    }
}
