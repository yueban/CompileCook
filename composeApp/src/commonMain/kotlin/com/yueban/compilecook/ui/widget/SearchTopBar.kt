package com.yueban.compilecook.ui.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.UniversalWidgetPreview
import com.yueban.compilecook.ui.util.preview.PreviewWrapper
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.common_des_back
import compilecook.composeapp.generated.resources.common_des_clear
import compilecook.composeapp.generated.resources.common_search_hint
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchTopBar(
  query: String,
  onQueryChange: (String) -> Unit,
  onBackClick: () -> Unit,
  onClearClick: () -> Unit,
  placeholderText: String = stringResource(Res.string.common_search_hint),
) {
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current

  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  TopAppBar(
    title = {
      TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
          Text(
            text = placeholderText,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colors.subTitleText
          )
        },
        singleLine = true,
        textStyle = AppTheme.typography.bodyLarge.copy(
          color = AppTheme.colors.titleText
        ),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = Color.Transparent,
          unfocusedContainerColor = Color.Transparent,
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent,
          cursorColor = AppTheme.colorScheme.primary
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(focusRequester)
      )
    },
    navigationIcon = {
      IconButton(onClick = onBackClick) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = stringResource(Res.string.common_des_back),
          tint = AppTheme.colors.titleText
        )
      }
    },
    actions = {
      if (query.isNotEmpty()) {
        IconButton(onClick = onClearClick) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(Res.string.common_des_clear),
            tint = AppTheme.colors.subTitleText
          )
        }
      }
    }
  )
}

@UniversalWidgetPreview
@Composable
private fun PreviewSearchTopBar() = PreviewWrapper {
  SearchTopBar(
    query = "",
    onQueryChange = {},
    onBackClick = {},
    onClearClick = {}
  )
}

@UniversalWidgetPreview
@Composable
private fun PreviewSearchTopBar_WithQuery() = PreviewWrapper {
  SearchTopBar(
    query = "搜索内容",
    onQueryChange = {},
    onBackClick = {},
    onClearClick = {}
  )
}
