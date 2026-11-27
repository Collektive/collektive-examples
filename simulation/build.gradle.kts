import java.awt.GraphicsEnvironment
import java.util.*

plugins {
    application
    alias(libs.plugins.multiJvmTesting)
    alias(libs.plugins.kotlin.jvm)
}

multiJvm {
    jvmVersionForCompilation.set(21)
}

dependencies {
    implementation(libs.bundles.alchemist)
    implementation(libs.bundles.collektive)
    runtimeOnly(libs.alchemist.kotlinscript) // Loads the *.alchemist.kts scenarios
    if (!GraphicsEnvironment.isHeadless()) {
        implementation("it.unibo.alchemist:alchemist-swingui:${libs.versions.alchemist.get()}")
    }
}

// Heap size estimation for batches
val maxHeap: Long? by project
val heap: Long =
    maxHeap ?: if (System.getProperty("os.name").lowercase().contains("linux")) {
        try {
            val memAvailableKb = ProcessBuilder("bash", "-c", "grep MemAvailable /proc/meminfo | grep -o '[0-9]*'")
                .redirectErrorStream(true)
                .start()
                .inputStream
                .bufferedReader()
                .use { it.readText().trim() }
                .toLong() / 1024
            println("Detected ${memAvailableKb}MB RAM available.")
            memAvailableKb * 9 / 10
        } catch (e: Exception) {
            println("Could not detect RAM, falling back to default. Error: ${e.message}")
            14 * 1024L
        }
    } else {
        14 * 1024L
    }

val taskSizeFromProject: Int? by project
val taskSize = taskSizeFromProject ?: 512
val threadCount = maxOf(1, minOf(Runtime.getRuntime().availableProcessors(), heap.toInt() / taskSize))
val alchemistGroupBatch = "Run batch simulations"
val alchemistGroupGraphic = "Run graphic simulations with Alchemist"

val runAllGraphic by tasks.register<DefaultTask>("runAllGraphic") {
    group = alchemistGroupGraphic
    description = "Launches all simulations with the graphic subsystem enabled"
}
val runAllBatch by tasks.register<DefaultTask>("runAllBatch") {
    group = alchemistGroupBatch
    description = "Launches all experiments"
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

// Options read by the scenarios (see it.unibo.collektive.examples.utils.LaunchOptions):
// Alchemist does not apply --override to Kotlin scripts, so they are passed as system properties.
val effectsProperty = "collektive.examples.effects"
val endTimeProperty = "collektive.examples.endTime"
val isCI = System.getenv("CI") == "true"
// Simulated end time: 2 in CI, otherwise optional (e.g., ./gradlew runGradientBatch -PendTime=2).
val endTime: String? = if (isCI) "2" else findProperty("endTime")?.toString()

File(rootProject.rootDir.path + "/simulation/src/main/kts")
    .listFiles()
    ?.filter { it.name.endsWith(".alchemist.kts") }
    ?.sortedBy { it.name }
    ?.forEach {
        val scenario = it.name.removeSuffix(".alchemist.kts")
        fun basetask(name: String, additionalConfiguration: JavaExec.() -> Unit = {}) = tasks.register<JavaExec>(name) {
            description = "Launches graphic simulation $scenario"
            mainClass.set("it.unibo.alchemist.Alchemist")
            classpath = sourceSets["main"].runtimeClasspath
            args("run", it.absolutePath)
            javaLauncher.set(
                javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(multiJvm.latestJava))
                },
            )
            endTime?.let { systemProperty(endTimeProperty, it) }
            if (!isCI) {
                this.additionalConfiguration()
            }
        }
        val capitalizedName = scenario.capitalizeString()
        val graphic by basetask("run${capitalizedName}Graphic") {
            group = alchemistGroupGraphic
            jvmArgs("-Dsun.java2d.opengl=false")
            systemProperty(effectsProperty, rootProject.file("effects/$scenario.json").absolutePath)
            args("--verbosity", "error")
        }
        runAllGraphic.dependsOn(graphic)
        val batch by basetask("run${capitalizedName}Batch") {
            group = alchemistGroupBatch
            description = "Launches batch experiments for $capitalizedName"
            maxHeapSize = "${minOf(heap.toInt(), Runtime.getRuntime().availableProcessors() * taskSize)}m"
            File("data").mkdirs()
        }
        runAllBatch.dependsOn(batch)
    }
