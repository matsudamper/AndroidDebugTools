@file:Suppress("UnstableApiUsage")

import groovy.lang.Closure

rootProject.name = "build-logic"

apply(from = "../dependency.gradle")

val applyMyDependency = extra["applyMyDependency"] as Closure<*>
applyMyDependency(settings)
