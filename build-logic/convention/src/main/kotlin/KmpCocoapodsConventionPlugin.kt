import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.targets.native.tasks.AbstractPodInstallTask

/**
 * Convention plugin that applies the Kotlin/Native CocoaPods plugin.
 *
 * This wrapper exists to avoid the "plugin already on classpath with unknown version" error
 * that occurs when declaring `org.jetbrains.kotlin.native.cocoapods` directly in
 * `libs.versions.toml` — the plugin ships bundled inside the Kotlin Gradle plugin and therefore
 * has no standalone version entry in the catalog.
 *
 * All pod install tasks are routed through scripts/pod-wrapper.sh, which uses `env -i` to strip
 * rvm's GEM_PATH from the environment. Without this, Homebrew Ruby 4.x picks up rvm's GEM_PATH
 * (pointing to ruby-3.x gem dirs) but cannot find bigdecimal, crashing pod install.
 */
class KmpCocoapodsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.native.cocoapods")
            val wrapper = rootProject.file("scripts/pod-wrapper.sh")
            afterEvaluate {
                tasks.withType(AbstractPodInstallTask::class.java).configureEach {
                    podExecutablePath.set(wrapper)
                }
            }
        }
    }
}
