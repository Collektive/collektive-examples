import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType
import org.jlleitschuh.gradle.ktlint.tasks.GenerateReportsTask

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.qa)
    alias(libs.plugins.multiJvmTesting) // Pre-configures the Java toolchains
    alias(libs.plugins.taskTree) // Helps debugging dependencies among gradle tasks
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.collektive)
}

val reportMerge by tasks.registering(ReportMergeTask::class) {
    output = project.layout.buildDirectory.file("reports/merge.sarif")
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
    plugins.withType<DetektPlugin> {
        val detektTasks = tasks.withType<Detekt>()
            .matching { task ->
                task.name.let { it.endsWith("Main") || it.endsWith("Test") } &&
                    !task.name.contains("Baseline")
            }
        val check by tasks.getting
        val detektAll by tasks.registering {
            group = "verification"
            check.dependsOn(this)
            dependsOn(detektTasks)
        }
    }

    // Enforce the use of the Kotlin version in all subprojects
    configurations.matching { it.name != "detekt" }.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion(rootProject.libs.versions.kotlin.asProvider().get())
            }
        }
    }

    tasks.withType<Detekt>().configureEach { finalizedBy(reportMerge) }
    tasks.withType<GenerateReportsTask>().configureEach { finalizedBy(reportMerge) }
    reportMerge {
        input.from(tasks.withType<Detekt>().map { it.sarifReportFile })
        input.from(tasks.withType<GenerateReportsTask>().flatMap { it.reportsOutputDirectory.asFileTree.files })
    }
}
