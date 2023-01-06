import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektGenerateConfigTask
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.reporting.ReportingExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

@Suppress("unused")
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            pluginManager.`apply`(DetektPlugin::class.java)
            dependencies {
                val detektPlugins = target.project.configurations.maybeCreate("detektPlugins")
                detektPlugins(libs.findLibrary("twitter.compose.rules.detekt").get())
                detektPlugins(libs.findLibrary("detekt.formatting").get())
            }
            extensions.getByType<DetektExtension>().apply {
                source = files(
                    "src/main/kotlin",
                    "src/main/java",
                )
                parallel = true
                buildUponDefaultConfig = true
                allRules = false
                disableDefaultRuleSets = true
                config.setFrom(file("${rootProject.projectDir}/detekt.config.yml"))
                debug = false
                ignoreFailures = false
                basePath = projectDir.path
            }
            tasks.withType<Detekt>().configureEach {
                reports {
                    html.required.set(false)
                    txt.required.set(true)
                    txt.outputLocation.set(file("build/reports/detekt.txt"))
                    sarif.required.set(false)
                    md.required.set(false)
                }
            }
        }
    }
}
