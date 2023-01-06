@file:Suppress("UnstableApiUsage")

import groovy.lang.Closure

rootProject.name = "build-conventions"

apply(from = "../dependency.gradle")

val applyMyDependency = extra["applyMyDependency"] as Closure<*>
applyMyDependency(settings)
