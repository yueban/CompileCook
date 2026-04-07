package com.yueban.compilecook.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val titleTextLight = Color(0xFF121417)
val titleTextDark = Color(0xFFE3E3E6)

val primaryButtonColors: ButtonColors
  @Composable
  get() = ButtonDefaults.buttonColors().copy(
    containerColor = AppTheme.colorScheme.primary,
    contentColor = AppTheme.colorScheme.onPrimary,
  )

val secondaryButtonColors: ButtonColors
  @Composable
  get() = ButtonDefaults.buttonColors().copy(
    containerColor = AppTheme.colorScheme.secondary,
    contentColor = AppTheme.colorScheme.onSecondary,
  )

val primaryContainerButtonColors: ButtonColors
  @Composable
  get() = ButtonDefaults.buttonColors().copy(
    containerColor = AppTheme.colorScheme.primaryContainer,
    contentColor = AppTheme.colorScheme.onPrimaryContainer,
  )

val secondaryContainerButtonColors: ButtonColors
  @Composable
  get() = ButtonDefaults.buttonColors().copy(
    containerColor = AppTheme.colorScheme.secondaryContainer,
    contentColor = AppTheme.colorScheme.onSecondaryContainer,
  )

val outlinedTextFieldColors: TextFieldColors
  @Composable
  get() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AppTheme.colorScheme.primary,
    unfocusedBorderColor = AppTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    cursorColor = AppTheme.colorScheme.primary,
    focusedLabelColor = AppTheme.colorScheme.primary
  )
