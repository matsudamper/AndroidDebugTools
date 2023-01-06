@file:Suppress("UnstableApiUsage")

import groovy.lang.Closure

apply(from = "dependency.gradle")

pluginManagement {
    includeBuild("build-conventions")
}

val applyMyDependency = extra["applyMyDependency"] as Closure<*>
applyMyDependency(settings)

rootProject.name = "android-debug-tools"

include(":adb")
include(":compose")
include(":config")
include("test_util")
