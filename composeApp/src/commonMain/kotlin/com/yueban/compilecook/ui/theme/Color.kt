package com.yueban.compilecook.ui.theme

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val titleTextLight = Color(0xFF121417)
val titleTextDark = Color(0xFFE3E3E6)

val primaryButtonColors: ButtonColors
  @Composable
  get() = ButtonDefaults.buttonColors().copy(
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
  )

val secondaryButtonColors: ButtonColors
  @Composable
  get() = ButtonDefaults.buttonColors().copy(
    containerColor = MaterialTheme.colorScheme.secondary,
    contentColor = MaterialTheme.colorScheme.onSecondary,
  )

val primaryContainerButtonColors: ButtonColors
  @Composable
  get() = ButtonDefaults.buttonColors().copy(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
  )

val secondaryContainerButtonColors: ButtonColors
  @Composable
  get() = ButtonDefaults.buttonColors().copy(
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
  )

val outlinedTextFieldColors: TextFieldColors
  @Composable
  get() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary
  )
