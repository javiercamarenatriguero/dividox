plugins {
    alias(libs.plugins.dividox.kmp.library)
    alias(libs.plugins.dividox.compose.multiplatform)
    alias(libs.plugins.dividox.kmp.ios)
    alias(libs.plugins.dividox.detekt)
}

compose.resources {
    publicResClass = true
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.materialIconsExtended)
        }
    }
}
