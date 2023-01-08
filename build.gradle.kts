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

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "Main"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            vendor = "matsudamper"
            packageName = "AndroidDebugTools"
            packageVersion = System.getProperty("VERSION")
                .takeIf { it?.isNotBlank() == true }
                ?: "1.0.0"
        }
    }
}
