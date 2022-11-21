@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }

    plugins {
        kotlin("multiplatform").version(extra["kotlin.version"] as String)
        id("org.jetbrains.compose").version(extra["compose.version"] as String)
    }
}


dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    versionCatalogs {
        create("libs") {
            version("kotlin", extra["kotlin.version"] as String)

            library("compose.material3.desktop","org.jetbrains.compose.material3:material3-desktop:1.2.2")

            library("kotlin.coroutine", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            library("kotlin.serialization.json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

            library("adam.adam", "com.malinskiy.adam:adam:0.4.7-SNAPSHOT")

            val kotestVersion = "5.5.4"
            library("kotest.runner.junit5", "io.kotest:kotest-runner-junit5:$kotestVersion")
        }
    }
}

rootProject.name = "android-debug-tools"

include(":adb")
include(":compose")
include(":config")