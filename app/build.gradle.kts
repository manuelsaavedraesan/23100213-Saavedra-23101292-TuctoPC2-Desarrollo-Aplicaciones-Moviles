
import java.util.Properties

val nasaProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.exists()) {
        localFile.inputStream().use { load(it) }
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.diarioespacial"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.diarioespacial"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        // API KEY DE NASA
        val nasaKey =
            nasaProperties.getProperty("NASA_API_KEY", "")

        buildConfigField(
            "String",
            "NASA_API_KEY",
            "\"$nasaKey\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {

    // ===== JETPACK COMPOSE =====
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // ===== ANDROID CORE =====
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // ===== NASA API - RETROFIT =====
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // ===== NAVEGACION =====
    implementation(
        "androidx.navigation:navigation-compose:2.10.1"
    )

    // ===== ROOM - BASE DE DATOS =====
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    // ===== VIEWMODEL =====
    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0"
    )

    // ===== IMAGENES APOD - COIL =====
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ===== ICONOS COMPOSE =====
    implementation(
        "androidx.compose.material:material-icons-core"
    )

    // ===== PRUEBAS =====
    testImplementation(libs.junit)

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )
    androidTestImplementation(
        libs.androidx.compose.ui.test.junit4
    )
    androidTestImplementation(
        libs.androidx.espresso.core
    )
    androidTestImplementation(libs.androidx.junit)

    // ===== DEBUG =====
    debugImplementation(
        libs.androidx.compose.ui.test.manifest
    )
    debugImplementation(
        libs.androidx.compose.ui.tooling
    )
}
