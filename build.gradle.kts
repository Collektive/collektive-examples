import java.awt.GraphicsEnvironment
import java.io.ByteArrayOutputStream
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

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
    if (!GraphicsEnvironment.isHeadless()) {
        implementation("it.unibo.alchemist:alchemist-swingui:${libs.versions.alchemist.get()}")
    }
}

multiJvm {
    jvmVersionForCompilation.set(latestJava)
}

// Heap size estimation for batches
val maxHeap: Long? by project
val heap: Long = maxHeap ?: if (System.getProperty("os.name").lowercase().contains("linux")) {
    ByteArrayOutputStream().use { output ->
        exec {
            executable = "bash"
            args = listOf("-c", "cat /proc/meminfo | grep MemAvailable | grep -o '[0-9]*'")
            standardOutput = output
        }
        output.toString().trim().toLong() / 1024
    }.also { println("Detected ${it}MB RAM available.") } * 9 / 10
} else {
    // Guess 16GB RAM of which 2 used by the OS
    14 * 1024L
}
val taskSizeFromProject: Int? by project
val taskSize = taskSizeFromProject ?: 512
val threadCount = maxOf(1, minOf(Runtime.getRuntime().availableProcessors(), heap.toInt() / taskSize))

val runAllGraphic by tasks.register<DefaultTask>("runAllGraphic") {
    group = alchemistGroup
    description = "Launches all simulations with the graphic subsystem enabled"
}
val runAllBatch by tasks.register<DefaultTask>("runAllBatch") {
    group = alchemistGroup
    description = "Launches all experiments"
}

val alchemistGroup = "Run Alchemist"

fun String.capitalizeString(): String =
    this.replaceFirstChar {
        if (it.isLowerCase()) {
            it.titlecase(
                Locale.getDefault(),
            )
        } else {
            it.toString()
        }
    }

File(rootProject.rootDir.path + "/src/main/yaml").listFiles()
    ?.filter { it.extension == "yml" }
    ?.sortedBy { it.nameWithoutExtension }
    ?.forEach {
        fun basetask(name: String, additionalConfiguration: JavaExec.() -> Unit = {}) = tasks.register<JavaExec>(name) {
            group = alchemistGroup
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
                args("--override", "terminate: { type: AfterTime, parameters: [20] } ")
            } else {
                this.additionalConfiguration()
            }
        }
        val capitalizedName = it.nameWithoutExtension.capitalizeString()
        val graphic by basetask("run${capitalizedName}Graphic") {
            args(
                "--override",
                "monitors: { type: SwingGUI, parameters: { graphics: effects/${it.nameWithoutExtension}.json } }",
                "--override",
                "launcher: { parameters: { batch: [], autoStart: false } }",
            )
        }
        runAllGraphic.dependsOn(graphic)
        val batch by basetask("run${capitalizedName}Batch") {
            description = "Launches batch experiments for $capitalizedName"
            maxHeapSize = "${minOf(heap.toInt(), Runtime.getRuntime().availableProcessors() * taskSize)}m"
            File("data").mkdirs()
        }
        runAllBatch.dependsOn(batch)
    }

tasks.withType(KotlinCompile::class).all {
    kotlinOptions.freeCompilerArgs = listOf("-Xcontext-receivers")
}
