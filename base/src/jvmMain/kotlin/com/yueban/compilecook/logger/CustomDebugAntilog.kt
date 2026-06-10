package com.yueban.compilecook.logger

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.logging.ConsoleHandler
import java.util.logging.Formatter
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.regex.Pattern

actual class CustomDebugAntilog actual constructor(
  private val minLogLevel: LogLevel,
) : Antilog() {
  private val defaultTag: String = "app"
  private val handler: List<Handler> = listOf()

  companion object {
    private const val CALL_STACK_INDEX = 8
  }

  private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault())

  private val consoleHandler: ConsoleHandler = ConsoleHandler().apply {
    level = Level.ALL
    formatter = object : Formatter() {
      override fun format(record: LogRecord): String {
        val time = dateFormatter.format(Instant.ofEpochMilli(record.millis))
        val throwable = record.thrown?.stackTraceToString() ?: ""
        return "$time ${record.message}\n$throwable"
      }
    }
  }

  private val logger: Logger = Logger.getLogger(CustomDebugAntilog::class.java.name).apply {
    level = Level.ALL

    if (handler.isEmpty()) {
      addHandler(consoleHandler)
      return@apply
    }
    handler.forEach {
      addHandler(it)
    }
  }.also { it.useParentHandlers = false }

  private val anonymousClass = Pattern.compile("(\\$\\d+)+$")

  private val tagMap: HashMap<LogLevel, String> = hashMapOf(
    LogLevel.VERBOSE to "[VERBOSE]",
    LogLevel.DEBUG to "[DEBUG]",
    LogLevel.INFO to "[INFO]",
    LogLevel.WARNING to "[WARN]",
    LogLevel.ERROR to "[ERROR]",
    LogLevel.ASSERT to "[ASSERT]",
  )

  override fun isEnable(priority: LogLevel, tag: String?) = priority >= minLogLevel

  actual override fun performLog(
    priority: LogLevel,
    tag: String?,
    throwable: Throwable?,
    message: String?,
  ) {
    val debugTag = tag ?: performTag(defaultTag)

    val fullMessage = if (message != null) {
      if (throwable != null) {
        "$message\n${throwable.stackTraceString}"
      } else {
        message
      }
    } else {
      throwable?.stackTraceString ?: return
    }

    when (priority) {
      LogLevel.VERBOSE -> logger.finest(buildLog(priority, debugTag, fullMessage))
      LogLevel.DEBUG -> logger.fine(buildLog(priority, debugTag, fullMessage))
      LogLevel.INFO -> logger.info(buildLog(priority, debugTag, fullMessage))
      LogLevel.WARNING -> logger.warning(buildLog(priority, debugTag, fullMessage))
      LogLevel.ERROR -> logger.severe(buildLog(priority, debugTag, fullMessage))
      LogLevel.ASSERT -> logger.severe(buildLog(priority, debugTag, fullMessage))
    }
  }

  internal fun buildLog(priority: LogLevel, tag: String?, message: String?): String {
    return "${tagMap[priority]} ${tag ?: performTag(defaultTag)} - $message"
  }

  private fun performTag(defaultTag: String): String {
    val thread = Thread.currentThread().stackTrace

    return if (thread.size >= CALL_STACK_INDEX) {
      thread[CALL_STACK_INDEX].run {
        "${createStackElementTag(className)}\$$methodName"
      }
    } else {
      defaultTag
    }
  }

  internal fun createStackElementTag(className: String): String {
    var tag = className
    val m = anonymousClass.matcher(tag)
    if (m.find()) {
      tag = m.replaceAll("")
    }
    return tag.substring(tag.lastIndexOf('.') + 1)
  }

  @Suppress("MagicNumber")
  private val Throwable.stackTraceString
    get(): String {

      val sw = StringWriter(256)
      val pw = PrintWriter(sw, false)
      printStackTrace(pw)
      pw.flush()
      return sw.toString()
    }
}
