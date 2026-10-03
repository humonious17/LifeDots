plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.lifedots"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.lifedots"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Supply these through your environment to build a distributable signed release.
    val releaseStore = providers.environmentVariable("LIFEDOTS_KEYSTORE").orNull
    signingConfigs {
        if (releaseStore != null) {
            create("distribution") {
                storeFile = file(releaseStore)
                storePassword = providers.environmentVariable("LIFEDOTS_STORE_PASSWORD").orNull
                keyAlias = providers.environmentVariable("LIFEDOTS_KEY_ALIAS").orNull
                keyPassword = providers.environmentVariable("LIFEDOTS_KEY_PASSWORD").orNull
            }
        }
    }

    buildTypes {
        // Installable local test build, separate from any differently signed existing app.
        create("sideload") {
            initWith(getByName("release"))
            applicationIdSuffix = ".sideload"
            versionNameSuffix = "-sideload"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            resValue("string", "app_name", "LifeDots Preview")
        }
        release {
            if (releaseStore != null) signingConfig = signingConfigs.getByName("distribution")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Gson for JSON serialization (goals feature)
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}