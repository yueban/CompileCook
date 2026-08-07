package com.yueban.compilecook.ui.util

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

val LocalNavAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

private const val IMAGE_SHARED_CONTENT_KEY_PREFIX = "image_"

/**
 * Creates a [SharedTransitionScope.SharedContentState] for an image whose key is derived from its
 * URL. Both the source (thumbnail) and the target (fullscreen preview) must produce the same key,
 * so construction is centralized here.
 */
@Composable
fun SharedTransitionScope.rememberImageSharedContentState(imageUrl: String): SharedTransitionScope.SharedContentState =
  rememberSharedContentState(key = IMAGE_SHARED_CONTENT_KEY_PREFIX + imageUrl)
