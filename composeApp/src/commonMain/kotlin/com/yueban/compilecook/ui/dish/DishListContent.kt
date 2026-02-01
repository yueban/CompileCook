package com.yueban.compilecook.ui.dish

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.theme.ExtendedTheme
import com.yueban.compilecook.ui.util.displayName
import com.yueban.compilecook.ui.util.emoji
import com.yueban.compilecook.ui.widget.EmptyComposable
import com.yueban.compilecook.ui.widget.TitleTopBar
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.dish_list_empty
import compilecook.composeapp.generated.resources.dish_list_item_difficulty
import compilecook.composeapp.generated.resources.dish_list_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun DishListContent(component: DishListComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()

  val title = state.dishCategory?.run { "$emoji $displayName" }
    ?: stringResource(Res.string.dish_list_title)

  Scaffold(
    topBar = {
      TitleTopBar(
        title = title,
        enableBack = true,
        onBackClick = component::onBackClicked
      )
    }
  ) { innerPadding ->
    AsyncContent(
      async = state.dishesAsync,
      emptyContent = {
        EmptyComposable(
          message = stringResource(Res.string.dish_list_empty),
          modifier = Modifier.padding(innerPadding)
        )
      }
    ) { dishes ->
      DishList(
        dishes = dishes,
        contentPadding = innerPadding,
        onDishClick = component::onDishClicked
      )
    }
  }
}

@Composable
private fun DishList(
  dishes: List<Dish>,
  contentPadding: PaddingValues,
  onDishClick: (dish: Dish) -> Unit,
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
      DishItem(dish = dish, onClick = { onDishClick(dish) })
    }
  }
}

@Composable
private fun DishItem(
  dish: Dish,
  onClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .height(110.dp)
      .clickable(onClick = onClick)
  ) {
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
            text = dish.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = ExtendedTheme.colors.titleText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = dish.description.replace("\n", " "), // Remove line breaks for list view
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
  }
}

@Composable
private fun DishImage(dish: Dish) {
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
      Text(
        text = dish.category.emoji,
        fontSize = 40.sp
      )
    }
  }
}

@Composable
private fun DifficultyStars(count: Int) {
  val stars = "⭐".repeat(count)
  Text(
    text = stars,
    style = MaterialTheme.typography.labelSmall,
    modifier = Modifier.padding(start = 2.dp)
  )
}
