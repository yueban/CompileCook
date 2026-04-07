package com.yueban.compilecook.ui.widget.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.MarkdownElement
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.rememberMarkdownState
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.PreviewConstant
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalScreenPreview

@Composable
fun CookMarkdown(
  state: State,
  modifier: Modifier = Modifier,
  listState: LazyListState,
) {
  Markdown(
    state = state,
    colors = cookMarkdownColors(),
    typography = cookMarkdownTypography(),
    components = cookMarkdownComponents(),
    // disable animation
    animations = markdownAnimations(animateTextSize = { this }),
    success = { successState, components, mod ->
      MarkdownSuccess(successState, listState, mod, components)
    },
    imageTransformer = Coil3ImageTransformerImpl,
    modifier = modifier.fillMaxSize(),
  )
}

@Composable
private fun MarkdownSuccess(
  state: State.Success,
  listState: LazyListState,
  modifier: Modifier,
  components: MarkdownComponents,
) {
  val nodes = remember(state.node) { state.node.children }
  LazyColumn(
    state = listState,
    modifier = modifier,
    contentPadding = PaddingValues(0.dp)
  ) {
    items(
      items = nodes,
      key = { node -> node.startOffset }
    ) { node ->
      MarkdownElement(node, components, state.content)
    }
  }
}

/**
 * Custom Colors matching ColorScheme
 */
@Composable
private fun cookMarkdownColors(): MarkdownColors {
  val colors = AppTheme.colors
  return markdownColor(
    text = AppTheme.colorScheme.onSurface,
    codeBackground = AppTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    inlineCodeBackground = AppTheme.colorScheme.surfaceVariant,
    dividerColor = colors.divider,
    // Make tables look clean
    tableBackground = AppTheme.colorScheme.surface,
  )
}

/**
 * Custom Typography
 * Maps standard Markdown headers to Material 3 styles, but tweaked for recipe reading.
 */
@Composable
private fun cookMarkdownTypography(): MarkdownTypography {
  val typography = AppTheme.typography
  val primaryColor = AppTheme.colorScheme.primary

  return markdownTypography(
    // H1 - Dish Title
    h1 = typography.headlineMedium.copy(
      fontWeight = FontWeight.Bold,
      color = AppTheme.colors.titleText
    ),
    // H2 - Section (Ingredients, Steps)
    h2 = typography.titleLarge.copy(
      fontWeight = FontWeight.Bold,
      color = AppTheme.colors.titleText
    ),
    // H3 - Sub-section
    h3 = typography.titleMedium.copy(
      fontWeight = FontWeight.Bold,
      color = AppTheme.colors.subTitleText
    ),
    // Normal text
    text = typography.bodyLarge.copy(
      color = AppTheme.colorScheme.onSurface
    ),
    // Bullet points (Ingredients)
    bullet = typography.bodyLarge,
    list = typography.bodyLarge,
    // Quotes (Tips/Notes) - Made italic and slightly lighter
    quote = typography.bodyMedium.copy(
      fontStyle = FontStyle.Italic,
      color = AppTheme.colors.bodyMedium
    ),
    // Code - Monospace
    code = TextStyle(
      fontFamily = FontFamily.Monospace,
      fontSize = typography.bodyMedium.fontSize,
      color = AppTheme.colorScheme.onSurfaceVariant
    ),
    // Links
    textLink = TextLinkStyles(
      style = SpanStyle(
        color = primaryColor,
        textDecoration = TextDecoration.Underline
      )
    )
  )
}

/**
 * Custom Components
 * Overrides specific renderers. Here we customize the Image renderer.
 */
@Composable
private fun cookMarkdownComponents() = markdownComponents(
  image = CustomImageComponent
)

/**
 * A custom Image component that adds Rounded Corners and fills width.
 * Perfect for Dish photos.
 */
private val CustomImageComponent: MarkdownComponent = { model ->
  val imageUrl = model.content

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp)
  ) {
    AsyncImage(
      model = imageUrl,
      contentDescription = null,
      modifier = Modifier
        .fillMaxWidth()
        .clip(AppTheme.shapes.medium)
        .border(
          width = 1.dp,
          color = AppTheme.colors.divider,
          shape = AppTheme.shapes.medium
        )
        .background(AppTheme.colorScheme.surfaceVariant),
      contentScale = ContentScale.FillWidth
    )
  }
}

@UniversalScreenPreview
@Composable
private fun PreviewCookMarkdown() = PreviewWrapper {
  val markdownState = rememberMarkdownState(
    content = PreviewConstant.dishDetail.content.trimIndent(),
  )
  val state by markdownState.state.collectAsState()
  CookMarkdown(state = state, listState = rememberLazyListState())
}
