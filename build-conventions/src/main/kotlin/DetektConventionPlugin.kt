import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

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


            val detekt = target.project.getTasksByName("detekt", false) as HashSet<*>
            (detekt.first() as Detekt).apply {
                source = files(
                    "src/main/kotlin",
                    "src/main/java",
                ).asFileTree
                parallel = true
                config.setFrom(files("${rootProject.projectDir}/detekt.config.yml"))
                buildUponDefaultConfig = true
                allRules = false
                baseline.set(file("path/to/baseline.xml"))
                disableDefaultRuleSets = true
                debug = false
                ignoreFailures = false
                basePath = projectDir.path
                reports {
                    xml.required.set(false)
                    xml.outputLocation.set(file("build/reports/detekt.xml"))
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
