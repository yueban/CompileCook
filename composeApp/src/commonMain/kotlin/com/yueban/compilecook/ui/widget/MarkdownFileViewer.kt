package com.yueban.compilecook.ui.widget

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.State
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.theme.EdgeToEdgeScreen

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
fun MarkdownViewer(content: String, modifier: Modifier = Modifier) {
  Markdown(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    content = content,
    imageTransformer = Coil3ImageTransformerImpl
  )
}

@Composable
fun MarkdownViewer(state: State, modifier: Modifier = Modifier) {
  Markdown(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState()),
    state = state,
    imageTransformer = Coil3ImageTransformerImpl
  )
}
