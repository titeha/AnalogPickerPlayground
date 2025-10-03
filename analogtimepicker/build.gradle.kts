plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
}

android {
  namespace = "dev.analog"          // было com.example.analogtimepicker
  compileSdk = 36

  defaultConfig {
    minSdk = 26
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-rules.pro")
  }

  // ВКЛЮЧАЕМ COMPOSE
  buildFeatures { compose = true }

  // Java/Kotlin 21 (как в основном модуле)
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
  }
  // новый DSL — можно так:
  kotlin {
    jvmToolchain(21)
    compilerOptions {
      // import org.jetbrains.kotlin.gradle.dsl.JvmTarget если подсвечивает
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
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
}

dependencies {
  // Используем Compose BOM и компоненты
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.activity.compose)

  // (ничего из appcompat/material не нужно)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
}