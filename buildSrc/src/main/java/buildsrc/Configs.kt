@file:Suppress("ConstPropertyName")

package buildsrc

import org.gradle.api.JavaVersion
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Configs {
  const val applicationId = "com.yueban.compilecook"
  const val compileSdk = 36
  const val minSdk = 29
  const val targetSdk = 36
  val versionCode: Int =
    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd", Locale.CHINA)).toInt()
  const val versionName = "0.0.1"

  const val testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

  val sourceCompatibility = JavaVersion.VERSION_21
  val targetCompatibility = JavaVersion.VERSION_21
  const val jvmToolchain = 21
}
