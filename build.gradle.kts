import buildsrc.Configs
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.composeHotReload) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.composeCompiler) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
  alias(libs.plugins.detekt)
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.sqldelight) apply false
}

val buildDirProvider: DirectoryProperty = project.layout.buildDirectory
val reportMerge by tasks.registering(ReportMergeTask::class) {
  output.set(buildDirProvider.get().asFile.resolve("reports/detekt/merge.xml"))
}
subprojects {
  apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

  detekt {
    toolVersion = rootProject.libs.versions.detekt.get()
    parallel = true
    config.setFrom("$rootDir/config/detekt/detekt.yml")
    buildUponDefaultConfig = true
    autoCorrect = true
  }

  dependencies {
    detektPlugins(rootProject.libs.detekt.formatting)
  }

  tasks.withType<Detekt>().configureEach {
    setSource(files(project.projectDir))
    exclude("**/build/**")
    reports {
      html.required.set(false)
      txt.required.set(true)
      txt.outputLocation.set(file("build/reports/detekt.txt"))
      xml.required.set(true)
      txt.outputLocation.set(file("build/reports/detekt.xml"))
      sarif.required.set(false)
    }
  }

  plugins.withType(DetektPlugin::class) {
    tasks.withType(Detekt::class) detekt@{
      finalizedBy(reportMerge)

      reportMerge.configure {
        input.from(this@detekt.xmlReportFile)
      }
    }
  }

  pluginManager.withPlugin(rootProject.libs.plugins.kotlinMultiplatform.get().pluginId) {
    configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
      jvmToolchain(Configs.jvmToolchain)

      compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
      }

      sourceSets.all {
        languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        languageSettings.optIn("kotlinx.coroutines.FlowPreview")
        languageSettings.optIn("kotlin.time.ExperimentalTime")
      }

      if (project.name == projects.composeApp.name) {
        sourceSets.all {
          languageSettings.optIn("coil3.annotation.ExperimentalCoilApi")
          languageSettings.optIn("com.arkivanov.decompose.DelicateDecomposeApi")
          languageSettings.optIn("com.arkivanov.decompose.ExperimentalDecomposeApi")
          languageSettings.optIn("com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi")
        }
      }
    }
  }
  pluginManager.withPlugin(rootProject.libs.plugins.composeMultiplatform.get().pluginId) {
    configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
      sourceSets.all {
        languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
      }
    }
  }
}
