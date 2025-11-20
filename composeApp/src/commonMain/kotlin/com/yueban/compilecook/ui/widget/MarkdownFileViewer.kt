package com.yueban.compilecook.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.theme.EdgeToEdgeScreen
import compilecook.composeapp.generated.resources.Res
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun MarkdownViewerPreview() = AppTheme {
  EdgeToEdgeScreen {
    MarkdownViewer(
      """
              # 小龙虾的做法
              
              ![成品](1)
              
              在家里做的小龙虾，肉质细嫩，鲜嫩多汁，干净卫生。
              
              预估烹饪难度：★★★★
              
              ## 必备原料和工具
              
              - 小龙虾
              - 油
              - 香叶
              - 八角
              - 桂皮
              - 青花椒
              - 花椒
              - 子弹头辣椒
              - 葱姜蒜
              - 郫县豆瓣
              - 黄豆酱
              - 啤酒
              - 生抽
              - 盐        
      """.trimIndent()
    )
  }
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
  MarkdownViewer(markdownContent)
}

@Composable
fun MarkdownViewer(content: String) {
  Markdown(
    content = content,
    imageTransformer = Coil3ImageTransformerImpl
  )
}
