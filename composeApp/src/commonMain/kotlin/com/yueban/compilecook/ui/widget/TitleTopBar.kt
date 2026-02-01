package com.yueban.compilecook.ui.widget

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.yueban.compilecook.ui.theme.AppTheme
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.common_des_back
import org.jetbrains.compose.resources.stringResource

@Composable
fun TitleTopBar(title: String, enableBack: Boolean = false, onBackClick: () -> Unit = {}) =
  CenterAlignedTopAppBar(
    title = {
      Text(
        text = title,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
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
    colors = TopAppBarDefaults.topAppBarColors().copy(
      containerColor = MaterialTheme.colorScheme.background,
      scrolledContainerColor = MaterialTheme.colorScheme.background,
    )
  )

@Preview
@Composable
private fun TitleTopBarPreview() {
  AppTheme {
    TitleTopBar(
      title = "Preview Title",
      enableBack = true,
    )
  }
}

@Preview
@Composable
private fun TitleTopBarNoBackPreview() {
  AppTheme {
    TitleTopBar(
      title = "Preview Title No Back",
      enableBack = false,
    )
  }
}
