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
            api(projects.common.currency)
            api(projects.common.network)
            api(projects.component.market)
            implementation(compose.materialIconsExtended)
            implementation(libs.kotlinx.datetime)
            implementation(libs.jetco.charts)
        }
    }
}
