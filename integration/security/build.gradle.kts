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
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(projects.component.portfolio)
            implementation(projects.component.market)
            implementation(projects.component.watchlist)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
