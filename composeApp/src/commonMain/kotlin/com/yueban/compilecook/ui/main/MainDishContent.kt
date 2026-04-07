package com.yueban.compilecook.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.repo.entity.DISH_DIFFICULTY_MAX_LEVEL
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.IconSource
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.util.asSource
import com.yueban.compilecook.ui.util.displayName
import com.yueban.compilecook.ui.util.icon
import com.yueban.compilecook.ui.util.preview.PreviewData
import com.yueban.compilecook.ui.util.preview.PreviewWrapper
import com.yueban.compilecook.ui.widget.AnyIcon
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.ic_difficulty_star
import compilecook.composeapp.generated.resources.main_dish_difficulty_format
import compilecook.composeapp.generated.resources.main_dish_favorite
import compilecook.composeapp.generated.resources.main_dish_section_categories
import compilecook.composeapp.generated.resources.main_dish_section_difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainDishContent(component: MainDishComponent, extraContentPaddingBottom: Dp) {
  val state by component.uiState.collectAsStateWithLifecycle()

  AsyncContent(
    async = state.dishCategoriesAsync,
    onRetry = component::onRetry
  ) { categories ->
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = AppTheme.dimens.categoryCardMinSize),
      contentPadding = PaddingValues(
        top = AppTheme.dimens.screenPadding,
        start = AppTheme.dimens.screenPadding,
        end = AppTheme.dimens.screenPadding,
        bottom = AppTheme.dimens.screenPadding + extraContentPaddingBottom
      ),
      horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.screenPadding),
      verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.screenPadding)
    ) {
      item(key = "dish_favorite", span = { GridItemSpan(maxLineSpan) }) {
        FavoriteCard(onClick = { component.onFavoriteClicked() })
      }

      item(key = "dish_categories", span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(stringResource(Res.string.main_dish_section_categories))
      }
      items(categories, key = { it.name }) { category ->
        category.displayName?.let { name ->
          DishCategoryCard(
            name = name,
            icon = category.icon.asSource(),
            onClick = { component.onDishCategoryClicked(category) }
          )
        }
      }

      item(span = { GridItemSpan(maxLineSpan) }) {
        SectionHeader(stringResource(Res.string.main_dish_section_difficulty))
      }
      items(DISH_DIFFICULTY_MAX_LEVEL, key = { "difficulty_$it" }) { index ->
        val level = index + 1
        DifficultyCard(
          level = level,
          onClick = { component.onDifficultyClicked(level) }
        )
      }
    }
  }
}

@Composable
private fun FavoriteCard(onClick: () -> Unit) {
  Card(
    shape = AppTheme.shapes.extraLarge,
    colors = CardDefaults.cardColors(containerColor = AppTheme.colorScheme.tertiaryContainer),
    elevation = CardDefaults.cardElevation(defaultElevation = AppTheme.dimens.elevationSmall),
    modifier = Modifier.fillMaxWidth().height(AppTheme.dimens.heroCardHeight),
    onClick = onClick,
  ) {
    Row(
      modifier = Modifier.fillMaxSize().padding(horizontal = AppTheme.dimens.heroCardHorizontalPadding),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier.size(AppTheme.dimens.categoryIconBox)
          .clip(CircleShape)
          .background(AppTheme.colorScheme.surface.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Favorite,
          contentDescription = stringResource(Res.string.main_dish_favorite),
          modifier = Modifier.size(AppTheme.dimens.heroCardIconSize).offset(y = AppTheme.dimens.favoriteIconOffsetY),
          tint = AppTheme.colors.favorite,
        )
      }

      Spacer(modifier = Modifier.width(AppTheme.dimens.heroCardIconGap))

      Text(
        modifier = Modifier.weight(1f),
        text = stringResource(Res.string.main_dish_favorite),
        style = AppTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = AppTheme.colorScheme.onTertiaryContainer,
      )

      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = AppTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
      )
    }
  }
}

@Composable
private fun SectionHeader(title: String) {
  Text(
    text = title,
    style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
    color = AppTheme.colors.titleText,
    modifier = Modifier.padding(top = AppTheme.dimens.smallGap, bottom = AppTheme.dimens.tinyGap)
  )
}

@Composable
@Suppress("MagicNumber")
private fun DishCategoryCard(
  name: String,
  icon: IconSource,
  onClick: () -> Unit,
) {
  Card(
    shape = AppTheme.shapes.large,
    colors = CardDefaults.cardColors(
      containerColor = AppTheme.colorScheme.surface,
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = AppTheme.dimens.elevationSmall),
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1f),
    onClick = onClick,
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(AppTheme.dimens.mediumGap),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(AppTheme.dimens.categoryIconBox)
          .clip(CircleShape),
        contentAlignment = Alignment.Center
      ) {
        AnyIcon(
          source = icon,
          contentDescription = name,
          modifier = Modifier.size(AppTheme.dimens.iconLarge),
        )
      }

      Spacer(modifier = Modifier.size(AppTheme.dimens.smallGap))

      Text(
        text = name,
        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
        color = AppTheme.colors.titleText,
        textAlign = TextAlign.Center,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun DifficultyCard(level: Int, onClick: () -> Unit) {
  Card(
    shape = AppTheme.shapes.large,
    colors = CardDefaults.cardColors(containerColor = AppTheme.colorScheme.surface),
    border = BorderStroke(AppTheme.dimens.borderThickness, AppTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1f),
    onClick = onClick,
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(AppTheme.dimens.mediumGap),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(AppTheme.dimens.categoryIconBox)
          .clip(CircleShape)
          .background(AppTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          val label = level.toString()
          Text(
            text = label,
            style = AppTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.colorScheme.onSecondaryContainer
          )
          Icon(
            painter = painterResource(Res.drawable.ic_difficulty_star),
            contentDescription = label,
            modifier = Modifier.size(AppTheme.dimens.iconSmall).offset(y = -AppTheme.dimens.difficultyStarOffsetY),
            tint = AppTheme.colors.difficultyStar
          )
        }
      }

      Spacer(modifier = Modifier.size(AppTheme.dimens.smallGap))

      Text(
        text = stringResource(Res.string.main_dish_difficulty_format, level),
        style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
        color = AppTheme.colors.titleText,
        textAlign = TextAlign.Center
      )
    }
  }
}

class PreviewMainDishComponent : MainDishComponent {
  override val uiState = MutableStateFlow(PreviewData.mainDishState)
  override fun onRetry() = Unit
  override fun onDishCategoryClicked(dishCategory: DishCategory) = Unit
  override fun onFavoriteClicked() = Unit
  override fun onDifficultyClicked(level: Int) = Unit
}

@UniversalScreenPreview
@Composable
private fun PreviewMainDishContent() = PreviewWrapper {
  MainDishContent(component = PreviewMainDishComponent(), extraContentPaddingBottom = AppTheme.dimens.zero)
}
