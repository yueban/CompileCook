import buildsrc.Configs
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.buildconfig)
}

buildConfig {
  useKotlinOutput { internalVisibility = false }
  className("BuildKonfig")
  packageName(Configs.packageName)

  buildConfigField("IS_DEBUG", Configs.DEBUG)
  buildConfigField("APP_ID", Configs.applicationId)
  buildConfigField("APP_VERSION", Configs.versionName)
  buildConfigField("APP_NAME", Configs.appName)
}

kotlin {
  android {
    namespace = "com.yueban.compilecook.base"
    compileSdk = Configs.compileSdk
    minSdk = Configs.minSdk

    compilerOptions {
      jvmTarget.set(JvmTarget.fromTarget(Configs.jvmTarget))
    }

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
      implementation(libs.openai.client)
    }
    androidMain.dependencies {
    }
    jvmMain.dependencies {
      implementation(libs.appdirs)
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
