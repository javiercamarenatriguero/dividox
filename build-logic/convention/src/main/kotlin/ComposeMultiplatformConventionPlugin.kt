import extensions.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.findPlugin("composeMultiplatform").get().get().pluginId)
                apply(libs.findPlugin("composeCompiler").get().get().pluginId)
                apply(libs.findPlugin("composeHotReload").get().get().pluginId)
            }

            val compose = extensions.getByType(ComposeExtension::class.java).dependencies

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        implementation(libs.findLibrary("compose-runtime").get())
                        implementation(libs.findLibrary("compose-foundation").get())
                        implementation(libs.findLibrary("compose-material3").get())
                        implementation(libs.findLibrary("compose-ui").get())
                        implementation(libs.findLibrary("compose-components-resources").get())
                        implementation(libs.findLibrary("compose-uiToolingPreview").get())
                        implementation(libs.findLibrary("androidx-lifecycle-viewmodelCompose").get())
                        implementation(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
                    }
                    androidMain.dependencies {
                        implementation(libs.findLibrary("compose-uiToolingPreview").get())
                        implementation(libs.findLibrary("androidx-activity-compose").get())
                    }
                }
            }

            dependencies.add("debugImplementation", libs.findLibrary("compose-uiTooling").get())
        }
    }
}
