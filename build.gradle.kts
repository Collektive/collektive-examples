import java.util.Locale
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(plugin = libs.plugins.kotlin.jvm.get().pluginId)

plugins {
    application
    alias(libs.plugins.multiJvmTesting) // Pre-configures the Java toolchains
    alias(libs.plugins.taskTree) // Helps debugging dependencies among gradle tasks
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.collektive)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.alchemist)
    implementation(libs.bundles.collektive)
}

multiJvm {
    jvmVersionForCompilation.set(latestJava)
}

val alchemistGroup = "Run Alchemist"

val runAll by tasks.register<DefaultTask>("runAll") {
    group = alchemistGroup
    description = "Launches all simulations"
}

fun String.capitalizeString(): String = this.replaceFirstChar {
    if (it.isLowerCase()) {
        it.titlecase(
            Locale.getDefault(),
        )
    } else {
        it.toString()
    }
}

/*
 * Scan the folder with the simulation files, and create a task for each one of them.
 */
File(rootProject.rootDir.path + "/src/main/yaml").listFiles()
    .orEmpty()
    .filter { it.extension == "yaml" }
    .sortedBy { it.nameWithoutExtension }
    .forEach {
        val task by tasks.register<JavaExec>("run${it.nameWithoutExtension.capitalizeString()}") {
            javaLauncher.set(
                javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(multiJvm.latestJava))
                },
            )
            group = alchemistGroup
            description = "Launches simulation ${it.nameWithoutExtension}"
            mainClass.set("it.unibo.alchemist.Alchemist")
            classpath = sourceSets["main"].runtimeClasspath
            val exportsDir = File("${projectDir.path}/build/exports/${it.nameWithoutExtension}")
            doFirst {
                if (!exportsDir.exists()) {
                    exportsDir.mkdirs()
                }
            }
            args("run", it.absolutePath)
            outputs.dir(exportsDir)
        }
        runAll.dependsOn(task)
    }

tasks.withType(KotlinCompile::class).all {
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
}
