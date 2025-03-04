import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass = "it.unibo.collektive.MainKt"
        }
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }
    js {
        nodejs {
            binaries.executable()
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.collektive)
            implementation(libs.collektive.stdlib)
            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.datetime)
            implementation(libs.mktt)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.serialization.protobuf)
        }
        jvmMain.dependencies {
            implementation(project.dependencies.platform("org.apache.logging.log4j:log4j-bom:2.24.3"))
            runtimeOnly("org.apache.logging.log4j:log4j-core")
            runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl")
            implementation(libs.bundles.hivemq)
            implementation(libs.kotlinx.coroutines.rx2)
            implementation(libs.kotlin.logging.jvm)
            implementation(libs.slf4j)
        }
    }
}
