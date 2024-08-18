plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.jetbrains.kotlin.android)
  alias(libs.plugins.com.google.devtools.ksp)
  alias(libs.plugins.com.google.dagger.hilt.android)
}

android {
  namespace = "com.a3.soundprofiles"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.a3.soundprofiles"
    minSdk = 29
    targetSdk = 34
    versionCode = 1
    versionName = "0.0.1-alpha"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    annotationProcessor(libs.androidx.room.compiler)
  ksp(libs.androidx.room.compiler)
  implementation(libs.androidx.room.ktx)
  implementation(libs.com.google.dagger.hilt.android)
  ksp(libs.com.google.dagger.hilt.compiler)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
}
