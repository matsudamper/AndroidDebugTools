@file:Suppress("UnstableApiUsage")

import groovy.lang.Closure

apply(from = "dependency.gradle")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

val applyMyDependencyResolutionManagement = extra["applyMyDependencyResolutionManagement"] as Closure<*>
applyMyDependencyResolutionManagement(settings)

rootProject.name = "android-debug-tools"

include(":adb")
include(":compose")
include(":config")
include("test_util")
