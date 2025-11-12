package com.yueban.compilecook.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.yueban.compilecook.logger.Logger
import compilecook.composeapp.generated.resources.Res
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun MarkdownFileViewerPreview() {
  MarkdownFileViewer("小龙虾.md")
}

@Composable
@Suppress("TooGenericExceptionCaught")
fun MarkdownFileViewer(filePath: String) {
  val markdownContent = produceState<String?>(initialValue = null, filePath) {
    try {
      val bytes = Res.readBytes("files/$filePath")
      value = bytes.decodeToString()
    } catch (e: Exception) {
      Logger.e(e)
      value = "Error: Could not load file.\n${e.message}"
    }
  }.value ?: return
  Markdown(
    content = markdownContent,
    imageTransformer = Coil3ImageTransformerImpl
  )
}
