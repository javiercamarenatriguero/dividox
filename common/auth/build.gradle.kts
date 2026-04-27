plugins {
    alias(libs.plugins.dividox.kmp.library)
    alias(libs.plugins.dividox.kmp.ios)
    alias(libs.plugins.dividox.kmp.test)
    alias(libs.plugins.dividox.detekt)
    alias(libs.plugins.dividox.kmp.cocoapods)
}

kotlin {
    jvm()

    cocoapods {
        summary = "Dividox authentication module"
        homepage = "https://dividox.app"
        version = "1.0"
        ios.deploymentTarget = "14.0"
        pod("FirebaseAuth") { version = "~> 11.0" }
        podfile = project.file("../../iosApp/Podfile")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        androidMain.dependencies {
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
            implementation(libs.kotlinx.coroutines.play.services)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
