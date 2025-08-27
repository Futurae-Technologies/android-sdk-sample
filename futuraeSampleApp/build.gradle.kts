import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("plugin.serialization")
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.google.services)
}

val getCommitCount: () -> Int = {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        standardOutput = stdout
    }
    stdout.toString().trim().toInt()
}

val getLatestGitTag: () -> String = {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "describe", "--tags", "--abbrev=0", "--match=v*")
        standardOutput = stdout
    }
    stdout.toString().trim()
}

val getCommitsSinceLastTag: () -> Int = {
    val latestTag = getLatestGitTag()
    val stdout = ByteArrayOutputStream()
    try {
        exec {
            commandLine("git", "rev-list", "$latestTag..HEAD", "--count")
            standardOutput = stdout
        }
    } catch (e: Exception) {
        println("Error executing git command: ${e.message}")
    }
    stdout.toString().trim().toIntOrNull() ?: 0
}

val getVersionName: () -> String = {
    val latestTag = getLatestGitTag().trim()
    val commitsSinceLastTag = getCommitsSinceLastTag()
    val versionName = "$latestTag.$commitsSinceLastTag"
    println("Version Name: $versionName")
    versionName
}

android {
    namespace = "com.futurae.sampleapp"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    defaultConfig {
        applicationId = "com.futurae.sampleapp"
        minSdk = 26
        targetSdk = 35
        versionCode = getCommitCount()
        versionName = getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "sdk_id", extra.properties["SDK_ID"] as String)
        resValue("string", "sdk_key", extra.properties["SDK_KEY"] as String)
        resValue("string", "base_url", extra.properties["BASE_URL"] as String)
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            manifestPlaceholders["appNameSuffix"] = " DEV"
            signingConfig = signingConfigs.getByName("debug")
            resValue("string", "app_name", "SampleDebug")
        }

        create("qa") {
            isMinifyEnabled = true
            applicationIdSuffix = ".qa"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release", "debug")
            resValue("string", "app_name", "SampleQA")
        }
        getByName("release") {
            isMinifyEnabled = true
            applicationIdSuffix = ".prod"
            signingConfig = signingConfigs.getByName("debug")
            resValue("string", "app_name", "Sample")
        }
    }

    packagingOptions {
        resources {
            excludes.add("META-INF/DEPENDENCIES")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") // any version higher, requires Kotlin v2
    implementation("com.futurae.sdk:adaptive:1.1.1-alpha")
    implementation("com.futurae.sdk:futuraekit:3.7.0")

    // Refer to BOM mapping page to verify individual app versions used
    // https://developer.android.com/develop/ui/compose/bom/bom-mapping
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.3.1")

    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")

    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
    implementation("com.google.accompanist:accompanist-permissions:0.37.0")
    implementation("com.google.code.gson:gson:2.11.0")

    // Third-party
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.github.chaosleung:pinview:1.4.4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}