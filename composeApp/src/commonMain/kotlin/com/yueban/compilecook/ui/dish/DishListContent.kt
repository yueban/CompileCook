package com.yueban.compilecook.ui.dish

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.yueban.compilecook.repo.entity.DishSummary
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.theme.ExtendedTheme
import com.yueban.compilecook.ui.util.PreviewData
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.util.displayName
import com.yueban.compilecook.ui.util.icon
import com.yueban.compilecook.ui.widget.EmptyComposable
import com.yueban.compilecook.ui.widget.FavoriteButton
import com.yueban.compilecook.ui.widget.SearchTopBar
import com.yueban.compilecook.ui.widget.TitleTopBar
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.dish_list_des_item_difficulty_format
import compilecook.composeapp.generated.resources.dish_list_difficulty_title_format
import compilecook.composeapp.generated.resources.dish_list_empty
import compilecook.composeapp.generated.resources.dish_list_favorite_empty
import compilecook.composeapp.generated.resources.dish_list_favorite_title
import compilecook.composeapp.generated.resources.dish_list_item_difficulty
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
          enableBack = true,
          onBackClick = component::onBackClicked,
          actions = {
            IconButton(onClick = { component.onSearchActiveChanged(true) }) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = ExtendedTheme.colors.titleText
              )
            }
          }
        )
      }
    }
  ) { innerPadding ->
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
          modifier = Modifier.padding(innerPadding)
        )
      }
    ) { dishes ->
      DishList(
        dishes = dishes,
        contentPadding = innerPadding,
        onDishClick = component::onDishClicked,
        onDishFavoriteClick = component::onDishFavoriteClick,
      )
    }
  }
}

@Composable
private fun DishList(
  dishes: List<DishSummary>,
  contentPadding: PaddingValues,
  onDishClick: (dish: DishSummary) -> Unit,
  onDishFavoriteClick: (dish: DishSummary) -> Unit,
) {
  LazyColumn(
    contentPadding = PaddingValues(
      top = contentPadding.calculateTopPadding() + 16.dp,
      bottom = contentPadding.calculateBottomPadding() + 16.dp,
      start = 16.dp,
      end = 16.dp
    ),
    verticalArrangement = Arrangement.spacedBy(12.dp)
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
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(110.dp),
    onClick = onClick,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Row(modifier = Modifier.fillMaxSize()) {
        DishImage(dish = dish)

        Column(
          modifier = Modifier
            .weight(1f)
            .padding(12.dp)
            .fillMaxHeight(),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              modifier = Modifier.padding(end = 30.dp),
              text = dish.name,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = ExtendedTheme.colors.titleText,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = dish.description.replace("\n", " "),
              style = MaterialTheme.typography.bodySmall,
              color = ExtendedTheme.colors.bodyMedium,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
              lineHeight = 16.sp
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = stringResource(Res.string.dish_list_item_difficulty),
              style = MaterialTheme.typography.labelSmall,
              color = ExtendedTheme.colors.subTitleText
            )
            DifficultyStars(count = dish.difficulty.toInt())
          }
        }
      }

      FavoriteButton(
        isFavorite = dish.isFavorite,
        onClick = onFavoriteClick,
        modifier = Modifier.align(Alignment.TopEnd).size(40.dp)
      )
    }
  }
}

@Composable
private fun DishImage(dish: DishSummary) {
  val modifier = Modifier
    .width(110.dp)
    .fillMaxHeight()
    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))

  if (dish.image.isNotBlank()) {
    AsyncImage(
      model = dish.image,
      contentDescription = dish.name,
      contentScale = ContentScale.Crop,
      modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant)
    )
  } else {
    Box(
      modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painterResource(dish.category.icon),
        contentDescription = dish.category.displayName,
        modifier = Modifier.size(48.dp),
        tint = Color.Unspecified
      )
    }
  }
}

@Composable
private fun DifficultyStars(count: Int) {
  Row(
    modifier = Modifier.padding(start = 2.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    repeat(count) {
      Icon(
        painter = painterResource(Res.drawable.ic_difficulty_star),
        contentDescription = stringResource(Res.string.dish_list_des_item_difficulty_format, count),
        modifier = Modifier.size(14.dp),
        tint = ExtendedTheme.colors.difficultyStar,
      )
      Spacer(Modifier.width(1.dp))
    }
  }
}

private abstract class PreviewDishListComponent : DishListComponent {
  override val uiState = MutableStateFlow(PreviewData.dishListState)
  override fun onBackClicked() = Unit
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
