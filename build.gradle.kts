import buildsrc.Configs
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.report.ReportMergeTask

plugins {
  alias(libs.plugins.androidApplication) apply false
  alias(libs.plugins.androidLibrary) apply false
  alias(libs.plugins.composeHotReload) apply false
  alias(libs.plugins.composeMultiplatform) apply false
  alias(libs.plugins.composeCompiler) apply false
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.detekt)
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
