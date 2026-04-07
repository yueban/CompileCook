package com.yueban.compilecook.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

val DefaultShapes: Shapes
  @Composable
  @ReadOnlyComposable
  get() = Shapes(
    extraSmall = RoundedCornerShape(AppTheme.dimens.radiusExtraSmall),
    small = RoundedCornerShape(AppTheme.dimens.radiusSmall),
    medium = RoundedCornerShape(AppTheme.dimens.radiusMedium),
    large = RoundedCornerShape(AppTheme.dimens.radiusLarge),
    extraLarge = RoundedCornerShape(AppTheme.dimens.radiusExtraLarge)
  )

val CornerBasedShape.startOnly: CornerBasedShape
  @Composable
  @ReadOnlyComposable
  get() = copy(topEnd = CornerSize(AppTheme.dimens.zero), bottomEnd = CornerSize(AppTheme.dimens.zero))

val CornerBasedShape.TopOnly: CornerBasedShape
  @Composable
  @ReadOnlyComposable
  get() = copy(bottomStart = CornerSize(AppTheme.dimens.zero), bottomEnd = CornerSize(AppTheme.dimens.zero))

val CornerBasedShape.EndOnly: CornerBasedShape
  @Composable
  @ReadOnlyComposable
  get() = copy(topStart = CornerSize(AppTheme.dimens.zero), bottomStart = CornerSize(AppTheme.dimens.zero))

val CornerBasedShape.BottomOnly: CornerBasedShape
  @Composable
  @ReadOnlyComposable
  get() = copy(topStart = CornerSize(AppTheme.dimens.zero), topEnd = CornerSize(AppTheme.dimens.zero))
