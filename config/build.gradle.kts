@Suppress("UnstableApiUsage")
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version 1.8.0
}

group = "net.matsudamper.android.debugtool.config"
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
        val jvmTest by getting
    }
}
