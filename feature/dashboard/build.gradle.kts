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
            implementation(project(":integration:security"))
            implementation(project(":component:watchlist"))
            implementation(project(":component:market"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(compose.materialIconsExtended)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":component:market"))
            implementation(project(":component:portfolio"))
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
        }
    }
}
