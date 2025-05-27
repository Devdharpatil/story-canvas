plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.storycanvas.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.storycanvas.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // For Kotlin 1.9.10 and Compose BOM 2024.04.01 (or 1.5.10/1.5.11 for Kotlin 1.9.22/1.9.23)
                                                 // Verify this against the official compatibility map based on your BOM's Compose UI version
                                                 // If Compose BOM 2024.04.01 -> Compose UI 1.6.5
                                                 // If Kotlin is 1.9.10, then for Compose UI 1.6.x, compiler is likely 1.5.3 or 1.5.4.
                                                 // Let's stick to 1.5.3 as per the error's hint for Kotlin 1.9.10
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core Android KTX & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose - BOM handles versions for other Compose libraries
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)             // uses alias: androidx-compose-ui
    implementation(libs.androidx.compose.ui.graphics)   // uses alias: androidx-compose-ui-graphics
    implementation(libs.androidx.compose.foundation)    // uses alias: androidx-compose-foundation
    implementation(libs.androidx.compose.material3)     // uses alias: androidx-compose-material3
    implementation(libs.androidx.compose.ui.tooling.preview) // uses alias: androidx-compose-ui-tooling-preview
    debugImplementation(libs.androidx.compose.ui.tooling)  // uses alias: androidx-compose-ui-tooling

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Navigation-Compose
    implementation(libs.androidx.navigation.compose)

    // Networking (Retrofit & OkHttp with Gson)
    implementation(libs.squareup.retrofit2.retrofit)
    implementation(libs.squareup.retrofit2.converter.gson)
    implementation(libs.squareup.okhttp3.okhttp)
    implementation(libs.squareup.okhttp3.logging.interceptor)

    // Image Loading (Coil)
    implementation(libs.coil.compose)

    // DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4) // uses alias: androidx-compose-ui-test-junit4
    debugImplementation(libs.androidx.compose.ui.test.manifest) // uses alias: androidx-compose-ui-test-manifest
}