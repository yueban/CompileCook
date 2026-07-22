@file:Suppress("ConstPropertyName")

package buildsrc

import org.gradle.api.JavaVersion
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Properties

object Configs {
  const val DEBUG = true

  const val packageName = "com.yueban.compilecook"
  const val applicationId = "com.yueban.compilecook"
  const val appName = "Compile Cook"
  const val compileSdk = 37
  const val minSdk = 29
  val versionCode: Int =
    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.CHINA)).toInt()
  const val versionName = "1.0.0"

  const val testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

  val sourceCompatibility = JavaVersion.VERSION_17
  val targetCompatibility = JavaVersion.VERSION_17
  const val jvmTarget = "17"
  const val jvmToolchain = 21

  val apiDomain: String get() = localProperties.getProperty("API_DOMAIN", "")
  val apiPath: String get() = localProperties.getProperty("API_PATH", "")
  val openAiApiToken: String get() = localProperties.getProperty("OPEN_AI_API_TOKEN", "")
  val openAiApiDomain: String get() = localProperties.getProperty("OPEN_AI_API_DOMAIN", "")
  val openAiApiPath: String get() = localProperties.getProperty("OPEN_AI_API_PATH", "")
  val openAiModel: String get() = localProperties.getProperty("OPEN_AI_MODEL", "")
}

private val localProperties: Properties
  get() = Properties().apply {
    val rootDir = System.getProperty("compilecook.project.root")
      ?: System.getProperty("user.dir")
    val file = File(rootDir, "local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
  }
