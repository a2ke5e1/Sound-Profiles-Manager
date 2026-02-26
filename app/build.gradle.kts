import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.kotlin.android)
  alias(libs.plugins.com.google.devtools.ksp)
  alias(libs.plugins.com.google.dagger.hilt.android)
  alias(libs.plugins.com.google.gms.google.services)
  alias(libs.plugins.com.google.firebase.crashlytics)
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
  compileSdk = 36

  defaultConfig {
    applicationId = "com.a3.soundprofiles"
    minSdk = 29
    targetSdk = 36
    versionCode = 15
    versionName = "0.0.8-alpha0${versionCode!! - 8}"

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
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")

      resValue("string", "admob_app_id", getEnvProperty("ADMOB_APPLICATION_ID", required = true))
      resValue("string", "admob_native_ad_unit_id", getEnvProperty("ADMOB_NATIVE_AD_UNIT", required = true))
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions { jvmTarget = "1.8" }
  buildFeatures {
    viewBinding = true
    buildConfig = true
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.material)
  implementation(libs.androidx.activity)
  implementation(libs.androidx.constraintlayout)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.livedata.ktx)
  implementation(libs.androidx.recyclerview.selection)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.play.services.ads)
  implementation(libs.firebase.crashlytics)
  implementation(libs.androidx.preference.ktx)
  annotationProcessor(libs.androidx.room.compiler)
  ksp(libs.androidx.room.compiler)
  implementation(libs.androidx.room.ktx)
  implementation(libs.com.google.dagger.hilt.android)
  ksp(libs.com.google.dagger.hilt.compiler)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
}
