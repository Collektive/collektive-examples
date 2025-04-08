plugins {
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting) // Pre-configures the Java toolchains
    alias(libs.plugins.taskTree) // Helps debugging dependencies among gradle tasks
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.collektive)
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }

    with(rootProject.libs.plugins) {
        apply(plugin = collektive.id)
        apply(plugin = taskTree.id)
        apply(plugin = kotlin.qa.id)
    }
}
