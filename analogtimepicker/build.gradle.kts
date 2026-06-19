plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.compose.compiler)
  id("maven-publish")
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

  // Публикуемый вариант (для JitPack/maven-publish) + исходники в артефакте.
  publishing {
    singleVariant("release") {
      withSourcesJar()
    }
  }
}

// Публикация AAR. Для многомодульного репозитория JitPack ждёт группу
// com.github.<user>.<repo>, поэтому координата: com.github.titeha.AnalogPickerPlayground:analogtimepicker:<версия>.
afterEvaluate {
  publishing {
    publications {
      register<MavenPublication>("release") {
        from(components["release"])
        groupId = "com.github.titeha.AnalogPickerPlayground"
        artifactId = "analogtimepicker"
        version = "0.1.0"
      }
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