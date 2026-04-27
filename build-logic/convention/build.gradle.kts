plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.tools.build.gradle)
    compileOnly(libs.kotlin.gradle)
    compileOnly(libs.compose.multiplatform.gradle)
    implementation(libs.detekt.gradle)
}

gradlePlugin {
    plugins {
        register("kmpApplication") {
            id = "com.akole.dividox.kmp.application"
            implementationClass = "KmpApplicationConventionPlugin"
        }
        register("kmpLibrary") {
            id = "com.akole.dividox.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("composeMultiplatform") {
            id = "com.akole.dividox.compose.multiplatform"
            implementationClass = "ComposeMultiplatformConventionPlugin"
        }
        register("kmpIos") {
            id = "com.akole.dividox.kmp.ios"
            implementationClass = "KmpIosConventionPlugin"
        }
        register("kmpTest") {
            id = "com.akole.dividox.kmp.test"
            implementationClass = "KmpTestConventionPlugin"
        }
        register("kmpDi") {
            id = "com.akole.dividox.kmp.di"
            implementationClass = "KmpDiConventionPlugin"
        }
        register("detekt") {
            id = "com.akole.dividox.detekt"
            implementationClass = "DetektConventionPlugin"
        }
        register("kmpCocoapods") {
            id = "com.akole.dividox.kmp.cocoapods"
            implementationClass = "KmpCocoapodsConventionPlugin"
        }
    }
}
