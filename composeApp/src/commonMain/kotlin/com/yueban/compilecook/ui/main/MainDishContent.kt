package com.yueban.compilecook.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.theme.ExtendedTheme
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.main_dish_category_aquatic
import compilecook.composeapp.generated.resources.main_dish_category_breakfast
import compilecook.composeapp.generated.resources.main_dish_category_condiment
import compilecook.composeapp.generated.resources.main_dish_category_dessert
import compilecook.composeapp.generated.resources.main_dish_category_drink
import compilecook.composeapp.generated.resources.main_dish_category_meat_dish
import compilecook.composeapp.generated.resources.main_dish_category_semi_finished
import compilecook.composeapp.generated.resources.main_dish_category_soup
import compilecook.composeapp.generated.resources.main_dish_category_staple
import compilecook.composeapp.generated.resources.main_dish_category_vegetable_dish
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainDishContent(component: MainDishComponent) {
  val state by component.uiState.collectAsStateWithLifecycle()

  AsyncContent(
    async = state.dishCategoriesAsync,
    onRetry = component::onRetry
  ) { categories ->
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 140.dp),
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(categories, key = { it.name }) { category ->
        getCategoryName(category)?.let { name ->
          DishCategoryCard(
            name = name,
            onClick = {
              // TODO: handle click event, goto dish list page
            }
          )
        }
      }
    }
  }
}

@Composable
@Suppress("MagicNumber")
private fun DishCategoryCard(
  name: String,
  onClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxSize()
      .aspectRatio(1.2f)
      .clickable(onClick = onClick)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = name.take(1),
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }

      Spacer(modifier = Modifier.size(12.dp))

      Text(
        text = name,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 16.sp
        ),
        color = ExtendedTheme.colors.titleText,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
private fun getCategoryName(category: DishCategory): String? =
  when (category) {
    DishCategory.VEGETABLE_DISH -> Res.string.main_dish_category_vegetable_dish
    DishCategory.MEAT_DISH -> Res.string.main_dish_category_meat_dish
    DishCategory.AQUATIC -> Res.string.main_dish_category_aquatic
    DishCategory.BREAKFAST -> Res.string.main_dish_category_breakfast
    DishCategory.STAPLE -> Res.string.main_dish_category_staple
    DishCategory.SEMI_FINISHED -> Res.string.main_dish_category_semi_finished
    DishCategory.SOUP -> Res.string.main_dish_category_soup
    DishCategory.DRINK -> Res.string.main_dish_category_drink
    DishCategory.CONDIMENT -> Res.string.main_dish_category_condiment
    DishCategory.DESSERT -> Res.string.main_dish_category_dessert
    DishCategory.UNKNOWN -> null
  }?.let { stringResource(it) }
