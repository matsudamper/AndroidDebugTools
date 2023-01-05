import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
}

group = "net.matsudamper.android.debugtool"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":adb"))
                implementation(project(":compose"))
                implementation(project(":config"))
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlin.coroutine)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    dependencies {
        configurations.maybeCreate("detektPlugins")
        detektPlugins(libs.twitter.compose.rules.detekt)
    }

    detekt {
        source = files(
            "src/main/kotlin",
            "src/main/java",
        )
        parallel = true
        config = files("${rootProject.projectDir}/detekt.config.yml")
        buildUponDefaultConfig = true
        allRules = false
        baseline = file("path/to/baseline.xml")
        disableDefaultRuleSets = true
        debug = false
        ignoreFailures = false
        basePath = projectDir.path
    }

    tasks.named<Detekt>("detekt").configure {
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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "Main"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            vendor = "matsudamper"
            packageName = "Android DebugTools"
            packageVersion = "1.0.0"
        }
    }
}
