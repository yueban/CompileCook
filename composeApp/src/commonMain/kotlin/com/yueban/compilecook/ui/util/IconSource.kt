package com.yueban.compilecook.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.DrawableResource

sealed interface IconSource {
  data class Resource(
    val res: DrawableResource,
    val tint: Color,
  ) : IconSource

  data class Vector(
    val imageVector: ImageVector,
    val tint: Color?,
  ) : IconSource
}

fun DrawableResource.asSource(tint: Color = Color.Unspecified) = IconSource.Resource(this, tint)

fun ImageVector.asSource(tint: Color? = null) = IconSource.Vector(this, tint)
