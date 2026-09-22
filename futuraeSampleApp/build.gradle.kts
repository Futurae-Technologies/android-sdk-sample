import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization")
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

val sdkVersionName = "3.9.7-rc1"
val adaptiveSdkVersionName = "1.1.2-alpha"

android {
    namespace = "com.futurae.sampleapp"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
        viewBinding = true
        compose = true
    }

    defaultConfig {
        applicationId = "com.futurae.sampleapp"
        minSdk = 26
        targetSdk = 36
        versionCode = getCommitCount()
        versionName = sdkVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val sdkId = project.findProperty("SDK_ID") as String
        val sdkKey = project.findProperty("SDK_KEY") as String
        val baseUrl = project.findProperty("BASE_URL") as String

        resValue("string", "sdk_id", sdkId)
        resValue("string", "sdk_key", sdkKey)
        resValue("string", "base_url", baseUrl)
        
        
        val cpn =  project.findProperty("CLOUD_PROJECT_NUMBER") as String? ?: "-"
        resValue("string", "cloud_project_number", "\"$cpn\"")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            manifestPlaceholders["appNameSuffix"] = " DEV"
            signingConfig = signingConfigs.getByName("debug")
            resValue("string", "app_name", "SampleDebug")
            buildConfigField("Boolean", "ENABLE_FLAG_SECURE", "false")
            buildConfigField("Boolean", "ENABLE_TEST_TAGS", "true")
        }

        create("qa") {
            isMinifyEnabled = true
            applicationIdSuffix = ".qa"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release", "debug")
            resValue("string", "app_name", "SampleQA")
            buildConfigField("Boolean", "ENABLE_FLAG_SECURE", "false")
            buildConfigField("Boolean", "ENABLE_TEST_TAGS", "true")
        }

        getByName("release") {
            isMinifyEnabled = true
            applicationIdSuffix = ".prod"
            signingConfig = signingConfigs.getByName("debug")
            resValue("string", "app_name", "Sample")
            buildConfigField("Boolean", "ENABLE_FLAG_SECURE", "true")
            buildConfigField("Boolean", "ENABLE_TEST_TAGS", "false")
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
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("com.futurae.sdk:adaptive:${adaptiveSdkVersionName}")
    implementation("com.futurae.sdk:futuraekit:${sdkVersionName}")

    // Refer to BOM mapping page to verify individual app versions used
    // https://developer.android.com/develop/ui/compose/bom/bom-mapping
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material3:material3:1.4.0")

    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.camera:camera-core:1.6.2")
    implementation("androidx.camera:camera-camera2:1.6.2")
    implementation("androidx.camera:camera-lifecycle:1.6.2")
    implementation("androidx.camera:camera-view:1.6.2")
    implementation("androidx.core:core-ktx:1.18.0")

    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.2")
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")
    implementation("com.google.code.gson:gson:2.14.0")

    // Third-party
    implementation("com.google.zxing:core:3.5.4")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.github.chaosleung:pinview:1.4.4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}