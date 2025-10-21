rootProject.name = "CompileCook"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
  plugins {
    id("de.fayard.refreshVersions") version "0.60.6"
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
  id("de.fayard.refreshVersions")
}

refreshVersions {
  file("build/tmp/refreshVersions").mkdirs()
  versionsPropertiesFile = file("build/tmp/refreshVersions/versions.properties")
}

include(":composeApp")
include(":base")
include(":data")
include(":repo")
