import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import java.io.File

/**
 * Isolated logic to inject macOS .lproj folders into a generated .app bundle.
 * This class is in buildSrc, so it is safe for the Configuration Cache.
 */
abstract class InjectMacLocalizationsAction : Action<Task> {

  @get:Input
  abstract val sourceDirPath: Property<String>

  @get:Input
  abstract val binariesDirPath: Property<String>

  override fun execute(task: Task) {
    val srcDir = File(sourceDirPath.get())
    val binariesDir = File(binariesDirPath.get())

    // 1. Find the .app bundle (walking the tree to handle different architectures)
    val appBundle = binariesDir.walkTopDown().firstOrNull { it.extension == "app" }

    if (appBundle != null) {
      val resourcesDir = File(appBundle, "Contents/Resources")

      // 2. Perform the copy
      if (srcDir.exists() && resourcesDir.exists()) {
        srcDir.copyRecursively(resourcesDir, overwrite = true)
        println("✅ [Injected] macOS localizations added to: ${appBundle.absolutePath}")
      } else {
        println("⚠️ [Skip] Missing source ($srcDir) or target ($resourcesDir)")
      }
    } else {
      println("⚠️ [Error] Could not find any .app bundle in $binariesDir")
    }
  }
}
