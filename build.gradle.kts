import buildsrc.Configs

plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.composeHotReload) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.composeCompiler) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
}

subprojects {
  plugins.withId("org.jetbrains.kotlin.multiplatform") {
    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
      jvmToolchain(Configs.jvmToolchain)

      sourceSets.all {
        languageSettings.optIn("kotlin.time.ExperimentalTime")
      }

      compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        optIn.add("org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi")
      }
    }
  }
}
