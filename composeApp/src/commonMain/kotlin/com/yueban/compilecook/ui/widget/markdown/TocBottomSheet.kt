package com.yueban.compilecook.ui.widget.markdown

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.mikepenz.markdown.model.State
import com.yueban.compilecook.ui.theme.ExtendedTheme
import com.yueban.compilecook.ui.util.PreviewData.dishState
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.common_toc_title
import org.jetbrains.compose.resources.stringResource

private val tocIndentPadding = 16.dp

@Composable
fun TocBottomSheet(
  toc: List<TocItem>,
  listState: LazyListState,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  onTocItemClick: (TocItem) -> Unit,
  onDismiss: () -> Unit,
) {
  val activeNodeIndex by remember(listState, toc) {
    derivedStateOf {
      val firstVisible = listState.firstVisibleItemIndex
      if (toc.isEmpty()) {
        -1
      } else {
        val lastPassedHeading = toc.lastOrNull { it.nodeIndex <= firstVisible }
        lastPassedHeading?.nodeIndex ?: toc.firstOrNull()?.nodeIndex ?: -1
      }
    }
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxWidth(),
      contentPadding = PaddingValues(
        start = 24.dp,
        end = 24.dp,
        top = 8.dp,
        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp
      )
    ) {
      item {
        Text(
          text = stringResource(Res.string.common_toc_title),
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = ExtendedTheme.colors.titleText,
          modifier = Modifier.padding(bottom = 16.dp)
        )
      }

      items(toc) { item ->
        val isActive = item.nodeIndex == activeNodeIndex
        val paddingStart = (item.level - 1) * tocIndentPadding

        Text(
          text = item.title,
          color = if (isActive) MaterialTheme.colorScheme.primary else ExtendedTheme.colors.titleText,
          fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onTocItemClick(item) }
            .padding(start = paddingStart, top = 12.dp, bottom = 12.dp),
        )
      }
    }
  }
}

@UniversalScreenPreview
@Composable
private fun PreviewTocBottomSheet() = PreviewWrapper {
  TocBottomSheet(
    toc = extractToc(dishState.contentAsync.value as State.Success),
    listState = rememberLazyListState(),
    sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.Expanded),
    onTocItemClick = { },
    onDismiss = {},
  )
}
