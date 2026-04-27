import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin that applies the Kotlin/Native CocoaPods plugin.
 *
 * This wrapper exists to avoid the "plugin already on classpath with unknown version" error
 * that occurs when declaring `org.jetbrains.kotlin.native.cocoapods` directly in
 * `libs.versions.toml` — the plugin ships bundled inside the Kotlin Gradle plugin and therefore
 * has no standalone version entry in the catalog.
 */
class KmpCocoapodsConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.native.cocoapods")
        }
    }
}
