import com.android.build.api.dsl.ApplicationExtension
import extensions.javaVersion
import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.findPlugin("kotlinMultiplatform").get().get().pluginId)
                apply(libs.findPlugin("androidApplication").get().get().pluginId)
            }

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget {
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_11)
                    }
                }
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
                defaultConfig {
                    minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
                    targetSdk = libs.findVersion("android-targetSdk").get().requiredVersion.toInt()
                }
                compileOptions {
                    sourceCompatibility = javaVersion
                    targetCompatibility = javaVersion
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
                configureSigningConfig(this@with, this)
                configureBuildTypes()
            }
        }
    }

    private fun configureSigningConfig(project: Project, ext: ApplicationExtension) {
        val properties = project.rootProject.properties
        ext.signingConfigs {
            val debugStore = project.file(properties["DEBUG_STORE_FILE"] as? String ?: "")
            if (debugStore.exists()) {
                getByName("debug") {
                    keyAlias = properties["DEBUG_KEY_ALIAS"] as String
                    keyPassword = properties["DEBUG_KEY_PASSWORD"] as String
                    storeFile = debugStore
                    storePassword = properties["DEBUG_STORE_PASSWORD"] as String
                }
            }
            val releaseStore = project.file(properties["RELEASE_STORE_FILE"] as? String ?: "")
            if (releaseStore.exists()) {
                create("release") {
                    keyAlias = properties["RELEASE_KEY_ALIAS"] as String
                    keyPassword = properties["RELEASE_KEY_PASSWORD"] as String
                    storeFile = releaseStore
                    storePassword = properties["RELEASE_STORE_PASSWORD"] as String
                }
            }
        }
    }

    private fun ApplicationExtension.configureBuildTypes() {
        buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
                isDebuggable = true
                signingConfig = signingConfigs.getByName("debug")
            }
            getByName("release") {
                isMinifyEnabled = true
                isDebuggable = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                signingConfig = signingConfigs.findByName("release")
            }
        }
    }
}
