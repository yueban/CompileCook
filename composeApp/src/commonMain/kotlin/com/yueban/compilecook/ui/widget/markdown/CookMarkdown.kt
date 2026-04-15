package com.yueban.compilecook.ui.widget.markdown

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.mikepenz.markdown.compose.LocalImageTransformer
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.compose.MarkdownElement
import com.mikepenz.markdown.compose.components.MarkdownComponentModel
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.rememberMarkdownState
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.LocalNavAnimatedVisibilityScope
import com.yueban.compilecook.ui.util.LocalSharedTransitionScope
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.util.preview.PreviewConstant
import com.yueban.compilecook.ui.util.preview.PreviewWrapper

private const val TRANSITION_DURATION = 300

/**
 * @param enableSharedElement disable shared element transitions on Markdown while overlay is visible to avoid duplicate
 *                            shared key conflicts.
 */
@Composable
fun CookMarkdown(
  state: State,
  modifier: Modifier = Modifier,
  listState: LazyListState,
  onImageClick: (String) -> Unit = {},
  enableSharedElement: Boolean,
) {
  Markdown(
    state = state,
    colors = cookMarkdownColors(),
    typography = cookMarkdownTypography(),
    components = cookMarkdownComponents(onImageClick, enableSharedElement),
    // disable content animation
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
    contentPadding = PaddingValues(AppTheme.dimens.elevationNone)
  ) {
    items(
      items = nodes,
      key = { node -> node.startOffset }
    ) { node ->
      MarkdownElement(node, components, state.content)
    }
  }
}

@Composable
private fun cookMarkdownColors(): MarkdownColors = with(AppTheme) {
  markdownColor(
    text = colorScheme.onSurface,
    codeBackground = colorScheme.surfaceVariant.copy(alpha = 0.5f),
    inlineCodeBackground = colorScheme.surfaceVariant,
    dividerColor = colors.divider,
    tableBackground = colorScheme.surface,
  )
}

@Composable
private fun cookMarkdownTypography(): MarkdownTypography = with(AppTheme) {
  return markdownTypography(
    h1 = typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = colors.titleText),
    h2 = typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = colors.titleText),
    h3 = typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = colors.subTitleText),
    text = typography.bodyLarge.copy(color = colorScheme.onSurface),
    bullet = typography.bodyLarge,
    list = typography.bodyLarge,
    quote = typography.bodyMedium.copy(fontStyle = FontStyle.Italic, color = colors.bodyMedium),
    code = TextStyle(
      fontFamily = FontFamily.Monospace,
      fontSize = typography.bodyMedium.fontSize,
      color = colorScheme.onSurfaceVariant
    ),
    textLink = TextLinkStyles(
      style = SpanStyle(
        color = colorScheme.primary,
        textDecoration = TextDecoration.Underline
      )
    )
  )
}

@Composable
private fun cookMarkdownComponents(
  onImageClick: (String) -> Unit,
  enableSharedElement: Boolean,
) = markdownComponents(
  image = { CustomImageComponent(model = it, onImageClick = onImageClick, enableSharedElement = enableSharedElement) },
  inlineImage = {
    CustomInlineImageComponent(
      model = it,
      onImageClick = onImageClick,
      enableSharedElement = enableSharedElement
    )
  },
)

@Composable
private fun CustomImageComponent(
  model: MarkdownComponentModel,
  onImageClick: (String) -> Unit,
  enableSharedElement: Boolean,
) {
  LocalImageTransformer.current.transform(model.content)?.let { imageData ->
    MarkdownImage(
      imageData = imageData,
      modifier = Modifier.fillMaxWidth(),
      imageUrl = model.content,
      onClick = { onImageClick(model.content) },
      enableSharedElement = enableSharedElement,
    )
  }
}

@Composable
private fun CustomInlineImageComponent(
  model: MarkdownComponentModel,
  onImageClick: (String) -> Unit,
  enableSharedElement: Boolean,
) {
  LocalImageTransformer.current.transform(model.content)?.let { imageData ->
    MarkdownImage(
      imageData = imageData,
      imageUrl = model.content,
      onClick = { onImageClick(model.content) },
      enableSharedElement = enableSharedElement,
    )
  }
}

@Composable
private fun MarkdownImage(
  imageData: ImageData,
  modifier: Modifier = Modifier,
  imageUrl: String,
  onClick: (() -> Unit),
  enableSharedElement: Boolean,
) {
  val sharedTransitionScope = LocalSharedTransitionScope.current
  val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

  val sharedElementModifier =
    if (enableSharedElement && sharedTransitionScope != null && animatedVisibilityScope != null) {
      with(sharedTransitionScope) {
        Modifier.sharedElement(
          rememberSharedContentState(key = "image_$imageUrl"),
          animatedVisibilityScope = animatedVisibilityScope,
          boundsTransform = { _, _ -> tween(durationMillis = TRANSITION_DURATION) }
        )
      }
    } else {
      Modifier
    }

  Image(
    painter = imageData.painter,
    contentDescription = imageData.contentDescription,
    modifier = modifier
      .height(AppTheme.dimens.markdownImageHeight)
      .padding(
        vertical = AppTheme.dimens.markdownImageVerticalPadding,
        horizontal = AppTheme.dimens.markdownImageHorizontalPadding
      )
      .clip(AppTheme.shapes.small)
      .clickable(onClick = onClick)
      .then(sharedElementModifier)
      .then(imageData.modifier),
    alignment = imageData.alignment,
    contentScale = ContentScale.Crop,
    alpha = imageData.alpha,
    colorFilter = imageData.colorFilter
  )
}

@UniversalScreenPreview
@Composable
private fun PreviewCookMarkdown() = PreviewWrapper {
  val markdownState = rememberMarkdownState(
    content = PreviewConstant.dishDetail.content.trimIndent(),
  )
  val state by markdownState.state.collectAsState()
  CookMarkdown(state = state, listState = rememberLazyListState(), enableSharedElement = true)
}
