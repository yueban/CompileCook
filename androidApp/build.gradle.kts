import buildsrc.Configs

plugins {
  alias(libs.plugins.androidApplication)
}

android {
  namespace = "com.yueban.compilecook"
  compileSdk = Configs.compileSdk

  defaultConfig {
    applicationId = Configs.applicationId
    minSdk = Configs.minSdk
    versionCode = Configs.versionCode
    versionName = Configs.versionName
  }
  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
  buildTypes {
    debug {
      isMinifyEnabled = false
      isShrinkResources = false
    }
    release {
      isMinifyEnabled = false
      isShrinkResources = false
    }
  }
  compileOptions {
    sourceCompatibility = Configs.sourceCompatibility
    targetCompatibility = Configs.targetCompatibility
  }
}

dependencies {
  implementation(projects.composeApp)
}
