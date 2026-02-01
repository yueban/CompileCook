package com.yueban.compilecook.ui.widget

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.yueban.compilecook.ui.theme.AppTheme
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.common_des_back
import org.jetbrains.compose.resources.stringResource

@Composable
fun TitleTopBar(
  title: String,
  enableBack: Boolean = false,
  onBackClick: () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
) {
  CenterAlignedTopAppBar(
    title = {
      Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleLarge
      )
    },
    navigationIcon = {
      if (enableBack) {
        IconButton(onClick = onBackClick) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(Res.string.common_des_back)
          )
        }
      }
    },
    actions = actions,
    colors = topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background,
      scrolledContainerColor = MaterialTheme.colorScheme.background,
      titleContentColor = MaterialTheme.colorScheme.onBackground,
      navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
      actionIconContentColor = MaterialTheme.colorScheme.onBackground
    )
  )
}

@Preview
@Composable
private fun PreviewTitleTopBar_Simple() {
  AppTheme {
    TitleTopBar(
      title = "Cookbook"
    )
  }
}

@Preview
@Composable
private fun PreviewTitleTopBar_WithBack() {
  AppTheme {
    TitleTopBar(
      title = "Vegetables",
      enableBack = true
    )
  }
}

@Preview
@Composable
private fun PreviewTitleTopBar_WithActions() {
  AppTheme {
    TitleTopBar(
      title = "All Dishes",
      enableBack = true,
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
}
