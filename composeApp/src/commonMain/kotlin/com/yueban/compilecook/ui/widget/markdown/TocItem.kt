package com.yueban.compilecook.ui.widget.markdown

import com.mikepenz.markdown.model.State
import kotlinx.serialization.Serializable
import org.intellij.markdown.MarkdownElementTypes

@Serializable
data class TocItem(
  val title: String,
  val level: Int,
  val nodeIndex: Int,
)

@Suppress("MagicNumber")
fun extractToc(state: State.Success): List<TocItem> {
  val toc = mutableListOf<TocItem>()
  state.node.children.forEachIndexed { index, child ->
    val level = when (child.type) {
      MarkdownElementTypes.ATX_1 -> 1
      MarkdownElementTypes.ATX_2 -> 2
      MarkdownElementTypes.ATX_3 -> 3
      MarkdownElementTypes.ATX_4 -> 4
      MarkdownElementTypes.ATX_5 -> 5
      MarkdownElementTypes.ATX_6 -> 6
      else -> null
    }
    if (level != null) {
      val rawText = state.content.substring(child.startOffset, child.endOffset)
      val title = rawText.replace(Regex("^#+\\s*"), "").trim()
      toc.add(TocItem(title, level, index))
    }
  }
  return toc
}
