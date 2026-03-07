import buildsrc.Configs
import buildsrc.NpmDeps
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.kotlin.serialization)
}

kotlin {
  android {
    namespace = "com.yueban.compilecook.data"
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
      implementation(projects.base)
      implementation(libs.sqldelight.coroutines.extensions)
      implementation(project.dependencies.platform(libs.ktor.bom))
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.content.negotiation)
      implementation(libs.ktor.serialization.kotlinx.json)
    }
    androidMain.dependencies {
      implementation(libs.sqldelight.android.driver)
      implementation(project.dependencies.platform(libs.ktor.bom))
      implementation(libs.ktor.client.okhttp)
    }
    nativeMain.dependencies {
      implementation(libs.sqldelight.native.driver)
      implementation(project.dependencies.platform(libs.ktor.bom))
      implementation(libs.ktor.client.darwin)
    }
    jvmMain.dependencies {
      implementation(libs.sqldelight.sqlite.driver)
      implementation(project.dependencies.platform(libs.ktor.bom))
      implementation(libs.ktor.client.okhttp)
    }
    wasmJsMain.dependencies {
      implementation(libs.sqldelight.web.driver)
      implementation(npm(NpmDeps.sqldelightJsWorker, libs.versions.sqldelight.get()))
      implementation(npm(NpmDeps.sqljs, libs.versions.sqljs.get()))
      implementation(devNpm(NpmDeps.webpack, libs.versions.webpack.get()))
      implementation(project.dependencies.platform(libs.ktor.bom))
      implementation(libs.ktor.client.js)
      implementation(libs.kotlin.browser)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }
    getByName("androidDeviceTest").dependencies {
      implementation(libs.androidx.test.runner)
      implementation(libs.androidx.test.core)
      implementation(libs.androidx.testExt.junit)
    }
    getByName("androidHostTest").dependencies {
      implementation(libs.sqldelight.sqlite.driver)
    }
    wasmJsTest.dependencies {
      implementation(libs.kotlinx.browser)
    }
  }
}

sqldelight {
  databases {
    create("AppDatabase") {
      packageName.set("com.yueban.compilecook.data.cache.db")
      generateAsync = true
    }
  }
}
