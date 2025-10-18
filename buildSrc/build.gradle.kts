repositories {
  google()
  mavenCentral()
}

plugins {
  `kotlin-dsl`
  groovy
}

dependencies {
  implementation(gradleApi())
  implementation(localGroovy())
}
