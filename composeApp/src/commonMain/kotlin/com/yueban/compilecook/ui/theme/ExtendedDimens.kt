@file:Suppress("MagicNumber")

package com.yueban.compilecook.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
data class ExtendedDimens(
  // global spacing
  val screenPadding: Dp = 16.dp,
  val extraLargeGap: Dp = 32.dp,
  val largeGap: Dp = 24.dp,
  val mediumGap: Dp = 12.dp,
  val smallGap: Dp = 8.dp,
  val tinyGap: Dp = 4.dp,
  val zero: Dp = 0.dp,

  // border & elevation
  val borderThickness: Dp = 1.dp,
  val elevationNone: Dp = 0.dp,
  val elevationExtraSmall: Dp = 1.dp,
  val elevationSmall: Dp = 2.dp,
  val elevationMedium: Dp = 3.dp,
  val elevationLarge: Dp = 4.dp,

  // corner radius
  val radiusExtraSmall: Dp = 4.dp,
  val radiusSmall: Dp = 8.dp,
  val radiusMedium: Dp = 12.dp,
  val radiusLarge: Dp = 16.dp,
  val radiusExtraLarge: Dp = 20.dp,

  // icon Sizes
  val iconExtraSmall: Dp = 14.dp,
  val iconSmall: Dp = 18.dp,
  val iconMedium: Dp = 24.dp,
  val iconLarge: Dp = 36.dp,
  val iconExtraLarge: Dp = 48.dp,
  val categoryIconBox: Dp = 64.dp,
  val heroCardIconSize: Dp = 32.dp,
  val dishCardFavoriteIconBox: Dp = 36.dp,

  // component specific
  val dishListItemHeight: Dp = 110.dp,
  val indicatorWidth: Dp = 4.dp,
  val heroCardHeight: Dp = 110.dp,
  val heroCardHorizontalPadding: Dp = 24.dp,
  val heroCardIconGap: Dp = 20.dp,
  val categoryCardMinSize: Dp = 140.dp,
  val dishCardMinSize: Dp = 160.dp,
  val dishCardImageAspectRatio: Float = 1f,
  val aboutIconSize: Dp = 100.dp,
  val fabSize: Dp = 56.dp,
  val fabIconSize: Dp = 28.dp,
  val fabContainerSize: Dp = 76.dp,
  val bottomFabOffset: Dp = 24.dp,

  // markdown
  val markdownImageHeight: Dp = 180.dp,
  val markdownImageVerticalPadding: Dp = 4.dp,
  val markdownImageHorizontalPadding: Dp = 2.dp,

  // alignment & offsets
  val favoriteIconOffsetY: Dp = 2.dp,
  val difficultyStarOffsetY: Dp = 2.dp,

  // ai chat
  val aiChatMessageMaxWidth: Dp = 300.dp,
  val aiChatInputFieldRadius: Dp = 24.dp,
  val aiChatLoadingSize: Dp = 16.dp,
  val aiChatLoadingStroke: Dp = 2.dp,
  val aiChatTopBarMinHeight: Dp = 48.dp,
  val aiChatImageThumbnailSize: Dp = 80.dp,
  val aiChatMessageImageHeight: Dp = 150.dp,
)

internal val LocalExtendedDimens = staticCompositionLocalOf { ExtendedDimens() }
