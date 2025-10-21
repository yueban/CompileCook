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

  js {
    browser()
    binaries.executable()
  }

  @OptIn(ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      api(libs.kotlin.stdlib)
      implementation(libs.napier)
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
