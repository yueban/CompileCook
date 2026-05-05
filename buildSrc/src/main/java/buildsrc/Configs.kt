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
  const val compileSdk = 36
  const val minSdk = 29
  val versionCode: Int =
    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.CHINA)).toInt()
  const val versionName = "1.0.0"

  const val testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

  val sourceCompatibility = JavaVersion.VERSION_17
  val targetCompatibility = JavaVersion.VERSION_17
  const val jvmTarget = "17"
  const val jvmToolchain = 21

  val mimoApiKey: String get() = localProperties.getProperty("MIMO_API_KEY", "")
  val mimoBaseUrl: String get() = localProperties.getProperty("MIMO_BASE_URL", "")
  val mimoModel: String get() = localProperties.getProperty("MIMO_MODEL", "")
}

private val localProperties: Properties by lazy {
  Properties().apply {
    val file = File(System.getProperty("user.dir"), "local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
  }
}
