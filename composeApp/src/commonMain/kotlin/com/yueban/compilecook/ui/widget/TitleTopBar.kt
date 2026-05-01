package com.yueban.compilecook.ui.widget

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.UniversalWidgetPreview
import com.yueban.compilecook.ui.util.preview.PreviewWrapper
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.app_name
import compilecook.composeapp.generated.resources.common_des_ai_assistant
import compilecook.composeapp.generated.resources.common_des_back
import org.jetbrains.compose.resources.stringResource

@Composable
fun TitleTopBar(
  title: String,
  onBackClick: (() -> Unit)? = null,
  onAiClick: (() -> Unit)? = null,
  actions: @Composable RowScope.() -> Unit = {},
) {
  CenterAlignedTopAppBar(
    title = {
      Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = AppTheme.typography.titleLarge
      )
    },
    navigationIcon = {
      if (onBackClick != null) {
        IconButton(onClick = onBackClick) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(Res.string.common_des_back)
          )
        }
      }
    },
    actions = {
      actions()
      if (onAiClick != null) {
        IconButton(onClick = onAiClick) {
          Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = stringResource(Res.string.common_des_ai_assistant),
          )
        }
      }
    },
    colors = topAppBarColors(
      containerColor = AppTheme.colorScheme.background,
      scrolledContainerColor = AppTheme.colorScheme.background,
      titleContentColor = AppTheme.colorScheme.onBackground,
      navigationIconContentColor = AppTheme.colorScheme.onBackground,
      actionIconContentColor = AppTheme.colorScheme.onBackground
    )
  )
}

@UniversalWidgetPreview
@Composable
private fun PreviewTitleTopBar_Simple() = PreviewWrapper {
  TitleTopBar(
    title = stringResource(Res.string.app_name)
  )
}

@UniversalWidgetPreview
@Composable
private fun PreviewTitleTopBar_WithBack() = PreviewWrapper {
  TitleTopBar(
    title = stringResource(Res.string.app_name),
    onBackClick = {},
  )
}

@UniversalWidgetPreview
@Composable
private fun PreviewTitleTopBar_WithActions() = PreviewWrapper {
  TitleTopBar(
    title = stringResource(Res.string.app_name),
    onBackClick = {},
    actions = {
      IconButton(onClick = {}) {
        Icon(Icons.Default.Search, contentDescription = "Search")
      }
      IconButton(onClick = {}) {
        Icon(Icons.Default.Settings, contentDescription = "Settings")
      }
    }
  )
}
