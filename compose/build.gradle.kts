plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "net.matsudamper.android.debugtool.compose"
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
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.material3.desktop)
            }
        }
        val jvmTest by getting
    }
}
