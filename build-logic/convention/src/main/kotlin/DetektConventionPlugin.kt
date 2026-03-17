import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig = true
                allRules = false
                parallel = true

                val configFile = rootProject.file("config/detekt/detekt.yml")
                if (configFile.exists()) {
                    config.setFrom(configFile)
                }
            }

            tasks.withType(io.gitlab.arturbosch.detekt.Detekt::class.java).configureEach {
                exclude { it.file.absolutePath.contains("/build/") }
                reports {
                    html.required.set(true)
                    sarif.required.set(true)
                    xml.required.set(false)
                }
            }
        }
    }
}
