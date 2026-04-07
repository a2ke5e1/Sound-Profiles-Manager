import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.com.google.gms.google.services)
    alias(libs.plugins.com.google.firebase.crashlytics)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.serialization)
}

val envProperties = Properties().apply {
    val envFile = rootProject.file("env.properties")
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
}

fun getEnvProperty(key: String, required: Boolean = false): String {
    val prop = envProperties.getProperty(key)
    if (prop == null && required) {
        throw GradleException("Property '$key' not found in env.properties. Please add it to root/env.properties")
    }
    return prop ?: ""
}

android {
    namespace = "com.a3.soundprofiles"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.a3.soundprofiles"
        minSdk = 29
        targetSdk = 36
        versionCode = 16
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(getEnvProperty("SIGNING_KEYSTORE_FILE"))
            storePassword = getEnvProperty("SIGNING_STORE_PASSWORD")
            keyAlias = getEnvProperty("SIGNING_KEY_ALIAS")
            keyPassword = getEnvProperty("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "admob_native_ad_unit_id", "ca-app-pub-3940256099942544/2247696110")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            resValue("string", "admob_app_id", getEnvProperty("ADMOB_APPLICATION_ID", required = true))
            resValue("string", "admob_native_ad_unit_id", getEnvProperty("ADMOB_NATIVE_AD_UNIT", required = true))
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
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
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.play.services.ads)
    implementation(libs.firebase.crashlytics)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)


    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.compose.ui.unit)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}