import buildsrc.Configs
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKotlinMultiplatformLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.composeHotReload)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.aboutLibraries)
}

kotlin {
  android {
    namespace = "com.yueban.compilecook.app"
    compileSdk = Configs.compileSdk
    minSdk = Configs.minSdk

    compilerOptions {
      jvmTarget.set(JvmTarget.fromTarget(Configs.jvmTarget))
    }

    androidResources {
      enable = true
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
      baseName = "ComposeApp"
      isStatic = true

      export(libs.decompose.decompose)
      export(libs.essenty.lifecycle)
      export(libs.essenty.backHandler)
      export(libs.essenty.stateKeeper)
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
      implementation(projects.repo)
      implementation(projects.base)

      // for iOS export
      api(libs.decompose.decompose)
      api(libs.essenty.lifecycle)
      api(libs.essenty.backHandler)
      api(libs.essenty.stateKeeper)

      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.compose.materialIconsExtended)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(project.dependencies.platform(libs.koin.bom))
      implementation(libs.koin.compose)
      implementation(libs.coil.compose)
      implementation(libs.coil.network.ktor3)
      implementation(libs.markdown.renderer)
      implementation(libs.markdown.renderer.m3)
      implementation(libs.decompose.extensions.compose)
      implementation(libs.decompose.extensions.compose.experimental)
      implementation(libs.aboutlibraries.core)
      implementation(libs.aboutlibraries.compose.m3)
      implementation(libs.platformtools.darkmodedetector)
    }
    androidMain.dependencies {
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.activity.compose)
      implementation(project.dependencies.platform(libs.ktor.bom))
      implementation(libs.ktor.client.okhttp)
    }
    nativeMain.dependencies {
      implementation(libs.sqldelight.native.driver)
      implementation(project.dependencies.platform(libs.ktor.bom))
      implementation(libs.ktor.client.darwin)
    }
    jvmMain.dependencies {
      implementation(compose.desktop.currentOs)
      implementation(libs.kotlinx.coroutines.swing)
      implementation(project.dependencies.platform(libs.ktor.bom))
      implementation(libs.ktor.client.okhttp)
    }
    wasmJsMain.dependencies {
      implementation(project.dependencies.platform(libs.ktor.bom))
      implementation(libs.ktor.client.js)
      implementation(libs.kotlin.browser)
      implementation(libs.kotlinx.browser)
    }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}

dependencies {
  androidRuntimeClasspath(libs.compose.uiTooling)
}

compose.desktop {
  application {
    mainClass = "com.yueban.compilecook.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = Configs.appName
      packageVersion = Configs.versionName

      modules("java.sql")

      macOS {
        iconFile.set(project.file("desktop_icons/macos_app_icon.icns"))
        bundleID = Configs.applicationId
        dmgPackageBuildVersion = Configs.versionCode.toString()

        infoPlist {
          extraKeysRawXml = """
            <key>LSHasLocalizedDisplayName</key>
            <true/>
          """.trimIndent()
        }
      }
      windows {
        iconFile.set(project.file("desktop_icons/win_app_icon.ico"))
      }
    }
  }
}

val macLprojPath: String = project.file("src/jvmMain/mac_lproj").absolutePath
val composeBinariesPath: String = layout.buildDirectory.dir("compose/binaries").get().asFile.absolutePath

tasks.configureEach {
  if (name.startsWith("package") && name.contains("Dmg")) {
    val action = objects.newInstance(InjectMacLocalizationsAction::class.java)

    action.sourceDirPath.set(macLprojPath)
    action.binariesDirPath.set(composeBinariesPath)

    doFirst(action)
  }
}

aboutLibraries {
  export {
    outputFile = file("src/commonMain/composeResources/files/aboutlibraries.json")
  }
}

tasks.matching {
  it.name.startsWith("wasmJs") && (it.name.contains("Webpack") || it.name.contains("Run"))
}.configureEach {
  dependsOn("generateProxyConfig")
}

tasks.register<JavaExec>("clearJvmData") {
  group = "application"
  description = "Clears local database and cache files for the JVM target."

  dependsOn("compileKotlinJvm")

  val jvmTarget = kotlin.targets.getByName("jvm")
  val mainCompilation = jvmTarget.compilations.getByName("main")
  classpath = mainCompilation.output.allOutputs + mainCompilation.runtimeDependencyFiles!!

  mainClass.set("com.yueban.compilecook.ClearJvmDataKt")
}

tasks.register("generateProxyConfig") {
  group = "build"
  description = "Generates webpack proxy config from shared API path constants."

  val outputFile = file("webpack.config.d/proxy.js")
  outputs.file(outputFile)

  doLast {
    outputFile.parentFile.mkdirs()
    outputFile.writeText(
      """
      |// Auto-generated by generateProxyConfig task. Do not edit manually.
      |config.devServer = config.devServer || {};
      |config.devServer.proxy = [
      |    {
      |        context: ['${Configs.apiPath}'],
      |        target: '${Configs.apiDomain}',
      |        secure: false,
      |        changeOrigin: true,
      |        logLevel: 'debug'
      |    },
      |    {
      |        context: ['${Configs.openAiApiPath}'],
      |        target: '${Configs.openAiApiDomain}',
      |        secure: false,
      |        changeOrigin: true,
      |        logLevel: 'debug'
      |    }
      |];
      """.trimMargin()
    )
  }
}
