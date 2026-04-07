package com.yueban.compilecook.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.repo.entity.TipSummary
import com.yueban.compilecook.repo.entity.TipType
import com.yueban.compilecook.repo.entity.TipType.ADVANCED
import com.yueban.compilecook.repo.entity.TipType.BASIC
import com.yueban.compilecook.repo.entity.TipType.LEARN
import com.yueban.compilecook.repo.entity.TipType.UNKNOWN
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.base.Fail
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.PreviewData
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.widget.EmptyComposable
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.main_tip_advanced
import compilecook.composeapp.generated.resources.main_tip_basic
import compilecook.composeapp.generated.resources.main_tip_empty
import compilecook.composeapp.generated.resources.main_tip_learn
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainTipContent(component: MainTipComponent, extraContentPaddingBottom: Dp) {
  val state by component.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(state.loadingAsync) {
    (state.loadingAsync as? Fail)?.let {
      component.showGlobalError(it.error)
    }
  }

  AsyncContent(
    async = state.groupedTipsAsync,
    onRetry = component::onRetry,
    emptyContent = { EmptyComposable(message = stringResource(Res.string.main_tip_empty)) },
  ) { groupedTips ->
    TipList(
      groupedTips = groupedTips,
      extraContentPaddingBottom = extraContentPaddingBottom,
      onItemClicked = { component.onTipClicked(it) }
    )
  }
}

@Composable
fun TipList(
  groupedTips: List<Pair<TipType, List<TipSummary>>>,
  extraContentPaddingBottom: Dp,
  onItemClicked: (TipSummary) -> Unit,
) {
  LazyColumn(
    contentPadding = PaddingValues(
      top = 16.dp,
      start = 16.dp,
      end = 16.dp,
      bottom = 16.dp + extraContentPaddingBottom
    ),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    groupedTips.forEach { (type, tips) ->
      item(key = type) {
        TipTypeHeader(type = type)
      }
      items(tips, key = { it.name }) { tip ->
        TipItem(tip = tip, onClick = { onItemClicked(tip) })
      }
    }
  }
}

@Composable
fun TipTypeHeader(type: TipType) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 16.dp, bottom = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(4.dp, 16.dp)
        .clip(AppTheme.shapes.extraSmall)
        .background(AppTheme.colorScheme.primary)
    )

    Spacer(modifier = Modifier.width(8.dp))

    Text(
      text = when (type) {
        BASIC -> Res.string.main_tip_basic
        LEARN -> Res.string.main_tip_learn
        ADVANCED -> Res.string.main_tip_advanced
        UNKNOWN -> null
      }?.let { stringResource(it) } ?: "",
      style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
      color = AppTheme.colors.titleText
    )
  }
}

@Composable
fun TipItem(tip: TipSummary, onClick: () -> Unit) {
  Card(
    shape = AppTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = AppTheme.colorScheme.surface,
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    onClick = onClick,
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = tip.name,
          style = AppTheme.typography.bodyLarge,
          color = AppTheme.colors.titleText
        )
      }

      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = tip.name,
        tint = AppTheme.colors.subTitleText.copy(alpha = 0.5f),
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

class PreviewMainTipComponent : MainTipComponent {
  override val uiState = MutableStateFlow(PreviewData.mainTipState)
  override fun onRetry() = Unit
  override fun onTipClicked(tip: TipSummary) = Unit
}

@UniversalScreenPreview
@Composable
private fun PreviewMainTipContent() = PreviewWrapper {
  MainTipContent(component = PreviewMainTipComponent(), extraContentPaddingBottom = 0.dp)
}
