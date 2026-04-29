plugins {
    alias(libs.plugins.dividox.kmp.library)
    alias(libs.plugins.dividox.kmp.ios)
    alias(libs.plugins.dividox.kmp.test)
    alias(libs.plugins.dividox.detekt)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.common.uiResources)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.datastore.preferences)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
