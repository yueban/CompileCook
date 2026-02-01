package com.yueban.compilecook

import com.yueban.compilecook.util.FileUtils
import java.io.File

/**
 * Standalone script to clean up local database and cache files.
 * Run via Gradle task: ./gradlew clearJvmData
 */
fun main() {
  println("--- Starting Cleanup jvm files ---")

  deleteDirectory(FileUtils.getUserCacheDir(), "Cache")
  deleteDirectory(FileUtils.getUserDataDir(), "Data")

  println("--- Cleanup Complete ---")
}

private fun deleteDirectory(dir: File, label: String) {
  if (dir.exists()) {
    val success = dir.deleteRecursively()
    if (success) {
      println("✅ Deleted $label dir: $dir")
    } else {
      System.err.println("❌ Failed to delete $label dir (File lock?): $dir")
    }
  } else {
    println("⚠️ $label dir not found (Nothing to clean): $dir")
  }
}
