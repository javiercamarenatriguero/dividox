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
                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = false
                    }
                }
            }
        }
    }
}
