package com.yueban.compilecook.util

import com.yueban.compilecook.BuildKonfig
import com.yueban.compilecook.logger.Logger
import net.harawata.appdirs.AppDirsFactory
import java.io.File

object FileUtils {
  fun getUserDataDir(): File = AppDirsFactory.getInstance()
    .getUserDataDir(BuildKonfig.APP_ID, BuildKonfig.APP_VERSION, null, false)
    .let { File(it) }
    .also { if (!it.exists()) it.mkdirs() }
    .also { Logger.d("userCacheDir: $it") }

  fun getUserCacheDir(): File = AppDirsFactory.getInstance()
    .getUserCacheDir(BuildKonfig.APP_ID, BuildKonfig.APP_VERSION, null)
    .let { File(it) }
    .also { if (!it.exists()) it.mkdirs() }
    .also { Logger.d("userDataDir: $it") }
}
