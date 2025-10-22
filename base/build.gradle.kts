import buildsrc.Configs
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
  androidLibrary {
    namespace = "com.yueban.compilecook.base"
    compileSdk = Configs.compileSdk
    minSdk = Configs.minSdk

    withHostTestBuilder {
    }

    withDeviceTestBuilder {
      sourceSetTreeName = "test"
    }.configure {
      instrumentationRunner = Configs.testInstrumentationRunner
    }
  }

  listOf(
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "baseKit"
    }
  }

  jvm()

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      api(libs.kotlin.stdlib)
      api(libs.kotlinx.coroutines.core)
      api(project.dependencies.platform(libs.koin.bom))
      api(libs.koin.core)
      api(libs.kotlinx.serialization)
      implementation(libs.napier)
      implementation(libs.coil.compose)
    }
    androidMain.dependencies {
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
    getByName("androidDeviceTest").dependencies {
      implementation(libs.androidx.test.runner)
      implementation(libs.androidx.test.core)
      implementation(libs.androidx.testExt.junit)
    }
  }
}
