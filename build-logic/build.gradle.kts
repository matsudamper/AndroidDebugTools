//import io.gitlab.arturbosch.detekt.Detekt

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(libs.kotlin.gradlePlugin)
    api(libs.detekt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("detektExec") {
            id = "conventions.detekt"
            implementationClass = "DetektConventionPlugin"
        }
    }
}
