plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "net.matsudamper.android.debugtool.adb"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlin.coroutine)
                implementation(libs.kotlin.serialization.json)
                api(libs.adam.adam)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(project(":test_util"))

                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
