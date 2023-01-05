plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "net.matsudamper.android.debugtool.test_util"
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
                implementation(libs.kotest.assertion.core)
            }
        }
    }
}
