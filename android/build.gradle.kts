plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinx.serialization)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(23))
    }
}

android {
    compileSdk =
        libs.versions.targetSdk
            .get()
            .toInt()
    namespace = "it.unibo.collektive"

    defaultConfig {
        applicationId = "it.unibo.collektive"
        minSdk =
            libs.versions.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources.excludes += "META-INF/*.md"
        resources.excludes += "META-INF/INDEX.LIST"
        resources.excludes += "META-INF/io.netty.versions.properties"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    viewBinding {
        enable = true
    }

    dataBinding {
        enable = true
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.material3)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.collektive)
    implementation(libs.collektive.stdlib)
    implementation(libs.kotlinx.serialization.protobuf)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.slf4j)
    implementation(libs.hivemq)
    implementation(libs.kotlinx.coroutines.rx2)
}
