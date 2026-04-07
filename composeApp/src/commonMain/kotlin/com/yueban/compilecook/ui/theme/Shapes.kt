package com.yueban.compilecook.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

internal val DefaultShapes = Shapes(
  extraSmall = RoundedCornerShape(2.dp),
  small = RoundedCornerShape(8.dp),
  medium = RoundedCornerShape(12.dp),
  large = RoundedCornerShape(16.dp),
  extraLarge = RoundedCornerShape(20.dp)
)

val CornerBasedShape.startOnly: CornerBasedShape
  get() = copy(topEnd = CornerSize(0.dp), bottomEnd = CornerSize(0.dp))

val CornerBasedShape.TopOnly: CornerBasedShape
  get() = copy(bottomStart = CornerSize(0.dp), bottomEnd = CornerSize(0.dp))

val CornerBasedShape.EndOnly: CornerBasedShape
  get() = copy(topStart = CornerSize(0.dp), bottomStart = CornerSize(0.dp))

val CornerBasedShape.BottomOnly: CornerBasedShape
  get() = copy(topStart = CornerSize(0.dp), topEnd = CornerSize(0.dp))
