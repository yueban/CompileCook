package com.yueban.compilecook.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.State
import com.yueban.compilecook.ui.theme.ExtendedTheme
import com.yueban.compilecook.ui.util.PreviewConstant
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalScreenPreview

@Composable
fun CookMarkdown(
  content: String,
  modifier: Modifier = Modifier,
) {
  Markdown(
    content = content,
    colors = cookMarkdownColors(),
    typography = cookMarkdownTypography(),
    components = cookMarkdownComponents(),
    imageTransformer = Coil3ImageTransformerImpl,
    modifier = modifier.fillMaxSize()
      .verticalScroll(rememberScrollState()),
  )
}

@Composable
fun CookMarkdown(
  state: State,
  modifier: Modifier = Modifier,
) {
  Markdown(
    state = state,
    colors = cookMarkdownColors(),
    typography = cookMarkdownTypography(),
    components = cookMarkdownComponents(),
    imageTransformer = Coil3ImageTransformerImpl,
    modifier = modifier.fillMaxSize()
      .verticalScroll(rememberScrollState()),
  )
}

/**
 * Custom Colors matching ExtendedTheme
 */
@Composable
private fun cookMarkdownColors(): MarkdownColors {
  val colors = ExtendedTheme.colors
  return markdownColor(
    text = MaterialTheme.colorScheme.onSurface,
    codeBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    inlineCodeBackground = MaterialTheme.colorScheme.surfaceVariant,
    dividerColor = colors.divider,
    // Make tables look clean
    tableBackground = MaterialTheme.colorScheme.surface,
  )
}

/**
 * Custom Typography
 * Maps standard Markdown headers to Material 3 styles, but tweaked for recipe reading.
 */
@Composable
private fun cookMarkdownTypography(): MarkdownTypography {
  val typography = MaterialTheme.typography
  val primaryColor = MaterialTheme.colorScheme.primary

  return markdownTypography(
    // H1 - Dish Title
    h1 = typography.headlineMedium.copy(
      fontWeight = FontWeight.Bold,
      color = ExtendedTheme.colors.titleText
    ),
    // H2 - Section (Ingredients, Steps)
    h2 = typography.titleLarge.copy(
      fontWeight = FontWeight.Bold,
      color = ExtendedTheme.colors.titleText
    ),
    // H3 - Sub-section
    h3 = typography.titleMedium.copy(
      fontWeight = FontWeight.Bold,
      color = ExtendedTheme.colors.subTitleText
    ),
    // Normal text
    text = typography.bodyLarge.copy(
      color = MaterialTheme.colorScheme.onSurface
    ),
    // Bullet points (Ingredients)
    bullet = typography.bodyLarge,
    list = typography.bodyLarge,
    // Quotes (Tips/Notes) - Made italic and slightly lighter
    quote = typography.bodyMedium.copy(
      fontStyle = FontStyle.Italic,
      color = ExtendedTheme.colors.bodyMedium
    ),
    // Code - Monospace
    code = TextStyle(
      fontFamily = FontFamily.Monospace,
      fontSize = typography.bodyMedium.fontSize,
      color = MaterialTheme.colorScheme.onSurfaceVariant
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
val CustomImageComponent: MarkdownComponent = { model ->
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
        .clip(RoundedCornerShape(12.dp))
        .border(
          width = 1.dp,
          color = ExtendedTheme.colors.divider,
          shape = RoundedCornerShape(12.dp)
        )
        .background(MaterialTheme.colorScheme.surfaceVariant),
      contentScale = ContentScale.FillWidth
    )
  }
}

@UniversalScreenPreview
@Composable
private fun PreviewCookMarkdown() = PreviewWrapper {
  CookMarkdown(content = PreviewConstant.dishDetail.content.trimIndent())
}
