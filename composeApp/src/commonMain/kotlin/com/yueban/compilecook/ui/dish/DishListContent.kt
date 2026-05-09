package com.yueban.compilecook.ui.dish

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.yueban.compilecook.repo.entity.DISH_DIFFICULTY_MAX_LEVEL
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.repo.entity.DishSummary
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.theme.AppTheme
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.util.displayName
import com.yueban.compilecook.ui.util.icon
import com.yueban.compilecook.ui.util.monochromeIcon
import com.yueban.compilecook.ui.util.preview.PreviewData
import com.yueban.compilecook.ui.util.preview.PreviewWrapper
import com.yueban.compilecook.ui.widget.EmptyComposable
import com.yueban.compilecook.ui.widget.SearchTopBar
import com.yueban.compilecook.ui.widget.TitleTopBar
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.common_des_toggle_favorite
import compilecook.composeapp.generated.resources.dish_list_des_item_difficulty_format
import compilecook.composeapp.generated.resources.dish_list_difficulty_title_format
import compilecook.composeapp.generated.resources.dish_list_empty
import compilecook.composeapp.generated.resources.dish_list_favorite_empty
import compilecook.composeapp.generated.resources.dish_list_favorite_title
import compilecook.composeapp.generated.resources.dish_list_filter_difficulty_format
import compilecook.composeapp.generated.resources.dish_list_search_hint_format
import compilecook.composeapp.generated.resources.dish_list_title
import compilecook.composeapp.generated.resources.ic_difficulty_star
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DishListContent(component: DishListComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()

  val title = when (val source = state.source) {
    DishListSource.All,
    DishListSource.Search,
    -> stringResource(Res.string.dish_list_title)
    is DishListSource.Category -> source.category.displayName ?: stringResource(Res.string.dish_list_title)
    is DishListSource.Difficulty -> stringResource(Res.string.dish_list_difficulty_title_format, source.level)
    DishListSource.Favorite -> stringResource(Res.string.dish_list_favorite_title)
  }

  Scaffold(
    topBar = {
      if (state.isSearchActive) {
        SearchTopBar(
          query = state.searchQuery,
          onQueryChange = component::onSearchQueryChanged,
          onBackClick = component::onBackClicked,
          onClearClick = { component.onSearchQueryChanged("") },
          placeholderText = stringResource(Res.string.dish_list_search_hint_format, title)
        )
      } else {
        TitleTopBar(
          title = title,
          onBackClick = component::onBackClicked,
          onAiClick = component::onAiClicked,
          actions = {
            IconButton(onClick = { component.onSearchActiveChanged(true) }) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = AppTheme.colors.titleText
              )
            }
          }
        )
      }
    }
  ) { innerPadding ->
    Column(Modifier.padding(innerPadding)) {
      if (state.source !is DishListSource.Category) {
        CategoryFilterBar(state = state, onCategoryToggle = component::onFilterCategoryChanged)
      }
      if (state.source !is DishListSource.Difficulty) {
        DifficultyFilterBar(state = state, onDifficultyToggle = component::onFilterDifficultyChanged)
      }
      AsyncContent(
        async = state.dishesAsync,
        emptyContent = {
          EmptyComposable(
            message = stringResource(
              if (state.source is DishListSource.Favorite) {
                Res.string.dish_list_favorite_empty
              } else {
                Res.string.dish_list_empty
              }
            ),
          )
        }
      ) { dishes ->
        DishList(
          dishes = dishes,
          onDishClick = component::onDishClicked,
          onDishFavoriteClick = component::onDishFavoriteClick,
        )
      }
    }
  }
}

@Composable
private fun CategoryFilterBar(
  state: DishListState,
  onCategoryToggle: (DishCategory?) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = AppTheme.dimens.screenPadding),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.smallGap)
  ) {
    DishCategory.entries.filter { it != DishCategory.UNKNOWN }.forEach { category ->
      val label = category.displayName ?: ""
      FilterChip(
        selected = state.filterCategory == category,
        onClick = { onCategoryToggle(if (state.filterCategory == category) null else category) },
        label = { Text(label) },
        leadingIcon = {
          Icon(
            painter = painterResource(category.monochromeIcon),
            contentDescription = label,
            modifier = Modifier.size(AppTheme.dimens.iconSmall)
          )
        },
      )
    }
  }
}

@Composable
private fun DifficultyFilterBar(
  state: DishListState,
  onDifficultyToggle: (Int?) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth()
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = AppTheme.dimens.screenPadding),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.smallGap)
  ) {
    for (level in 1..DISH_DIFFICULTY_MAX_LEVEL) {
      val label = stringResource(Res.string.dish_list_filter_difficulty_format, level)
      FilterChip(
        selected = state.filterDifficulty == level,
        onClick = { onDifficultyToggle(if (state.filterDifficulty == level) null else level) },
        label = { Text(label) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = label,
            modifier = Modifier.size(AppTheme.dimens.iconSmall)
          )
        },
      )
    }
  }
}

@Composable
private fun DishList(
  dishes: List<DishSummary>,
  onDishClick: (dish: DishSummary) -> Unit,
  onDishFavoriteClick: (dish: DishSummary) -> Unit,
) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = AppTheme.dimens.dishCardMinSize),
    contentPadding = PaddingValues(AppTheme.dimens.screenPadding),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.dimens.mediumGap),
    verticalArrangement = Arrangement.spacedBy(AppTheme.dimens.mediumGap)
  ) {
    items(dishes, key = { it.name }) { dish ->
      DishItem(
        dish = dish,
        onClick = { onDishClick(dish) },
        onFavoriteClick = { onDishFavoriteClick(dish) }
      )
    }
  }
}

@Composable
private fun DishItem(
  dish: DishSummary,
  onClick: () -> Unit,
  onFavoriteClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(AppTheme.shapes.medium)
      .clickable(onClick = onClick)
      .padding(bottom = AppTheme.dimens.smallGap)
  ) {
    Box {
      DishImage(dish = dish)

      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(AppTheme.dimens.smallGap)
          .size(AppTheme.dimens.dishCardFavoriteIconBox)
          .clip(CircleShape)
          .background(AppTheme.colorScheme.surface.copy(alpha = 0.6f))
          .clickable(onClick = onFavoriteClick),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (dish.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
          contentDescription = stringResource(Res.string.common_des_toggle_favorite),
          tint = if (dish.isFavorite) AppTheme.colors.favorite else AppTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(AppTheme.dimens.iconSmall)
        )
      }
    }

    Spacer(modifier = Modifier.height(AppTheme.dimens.smallGap))

    Column(
      modifier = Modifier.padding(horizontal = AppTheme.dimens.smallGap)
    ) {
      Text(
        text = dish.name,
        style = AppTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = AppTheme.colors.titleText,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      Text(
        text = dish.description.replace("\n", " "),
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colors.bodyMedium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      DifficultyStars(
        count = dish.difficulty.toInt(),
        modifier = Modifier.padding(top = AppTheme.dimens.tinyGap),
      )
    }
  }
}

@Composable
private fun DishImage(dish: DishSummary) {
  val modifier = Modifier
    .fillMaxWidth()
    .aspectRatio(AppTheme.dimens.dishCardImageAspectRatio)
    .clip(AppTheme.shapes.medium)

  if (dish.image.isNotBlank()) {
    AsyncImage(
      model = dish.image,
      contentDescription = dish.name,
      contentScale = ContentScale.Crop,
      modifier = modifier.background(AppTheme.colorScheme.surfaceVariant)
    )
  } else {
    Box(
      modifier = modifier.background(AppTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painterResource(dish.category.icon),
        contentDescription = dish.category.displayName,
        modifier = Modifier.size(AppTheme.dimens.iconExtraLarge),
        tint = Color.Unspecified
      )
    }
  }
}

@Composable
private fun DifficultyStars(count: Int, modifier: Modifier = Modifier) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    repeat(count) {
      Icon(
        painter = painterResource(Res.drawable.ic_difficulty_star),
        contentDescription = stringResource(Res.string.dish_list_des_item_difficulty_format, count),
        modifier = Modifier.size(AppTheme.dimens.iconExtraSmall),
        tint = AppTheme.colors.difficultyStar,
      )
      Spacer(Modifier.width(AppTheme.dimens.borderThickness))
    }
  }
}

private abstract class PreviewDishListComponent : DishListComponent {
  override val uiState = MutableStateFlow(PreviewData.dishListState)
  override fun onBackClicked() = Unit
  override fun onAiClicked() = Unit
  override fun onFilterCategoryChanged(category: DishCategory?) = Unit
  override fun onFilterDifficultyChanged(level: Int?) = Unit
  override fun onDishClicked(dish: DishSummary) = Unit
  override fun onDishFavoriteClick(dish: DishSummary) = Unit
  override fun onSearchActiveChanged(active: Boolean) = Unit
  override fun onSearchQueryChanged(query: String) = Unit
}

private class PreviewDishListSearchComponent : PreviewDishListComponent() {
  override val uiState = MutableStateFlow(PreviewData.dishListSearchState)
}

private class PreviewDishListEmptyComponent : PreviewDishListComponent() {
  override val uiState = MutableStateFlow(PreviewData.dishListEmptyState)
}

private class PreviewDishListFavoriteEmptyComponent : PreviewDishListComponent() {
  override val uiState = MutableStateFlow(PreviewData.dishListFavoriteEmptyState)
}

@UniversalScreenPreview
@Composable
private fun PreviewDishListContent() = PreviewWrapper {
  DishListContent(component = object : PreviewDishListComponent() {})
}

@UniversalScreenPreview
@Composable
private fun PreviewDishListContent_Search() = PreviewWrapper {
  DishListContent(component = PreviewDishListSearchComponent())
}

@UniversalScreenPreview
@Composable
private fun PreviewDishListContent_Empty() = PreviewWrapper {
  DishListContent(component = PreviewDishListEmptyComponent())
}

@UniversalScreenPreview
@Composable
private fun PreviewDishListContent_FavoriteEmpty() = PreviewWrapper {
  DishListContent(component = PreviewDishListFavoriteEmptyComponent())
}
