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

File(rootProject.rootDir.path + "/simulation/src/main/yaml")
    .listFiles()
    ?.filter { it.extension == "yml" }
    ?.sortedBy { it.nameWithoutExtension }
    ?.forEach {
        fun basetask(name: String, additionalConfiguration: JavaExec.() -> Unit = {}) = tasks.register<JavaExec>(name) {
            description = "Launches graphic simulation ${it.nameWithoutExtension}"
            mainClass.set("it.unibo.alchemist.Alchemist")
            classpath = sourceSets["main"].runtimeClasspath
            args("run", it.absolutePath)
            javaLauncher.set(
                javaToolchains.launcherFor {
                    languageVersion.set(JavaLanguageVersion.of(multiJvm.latestJava))
                },
            )
            if (System.getenv("CI") == "true") {
                args("--override", "terminate: { type: AfterTime, parameters: [2] } ")
            } else {
                this.additionalConfiguration()
            }
        }
        val capitalizedName = it.nameWithoutExtension.capitalizeString()
        val graphic by basetask("run${capitalizedName}Graphic") {
            group = alchemistGroupGraphic
            jvmArgs("-Dsun.java2d.opengl=false")
            args(
                "--override",
                "monitors: { type: SwingGUI, parameters: { graphics: ../effects/${it.nameWithoutExtension}.json } }",
                "--override",
                "launcher: { parameters: { batch: [], autoStart: false } }",
                "--verbosity",
                "error",
            )
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
