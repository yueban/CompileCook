package com.yueban.compilecook.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.repo.entity.DishCategory
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.theme.ExtendedTheme
import com.yueban.compilecook.ui.util.PreviewData
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.util.displayName
import com.yueban.compilecook.ui.util.icon
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun MainDishContent(component: MainDishComponent, extraContentPaddingBottom: Dp) {
  val state by component.uiState.collectAsStateWithLifecycle()

  AsyncContent(
    async = state.dishCategoriesAsync,
    onRetry = component::onRetry
  ) { categories ->
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 140.dp),
      contentPadding = PaddingValues(
        top = 16.dp,
        start = 16.dp,
        end = 16.dp,
        bottom = 16.dp + extraContentPaddingBottom
      ),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      items(categories, key = { it.name }) { category ->
        category.displayName?.let { name ->
          DishCategoryCard(
            name = name,
            icon = category.icon,
            onClick = { component.onDishCategoryClicked(category) }
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
  icon: DrawableResource,
  onClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface,
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1f)
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
          .size(64.dp)
          .clip(CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(icon),
          contentDescription = name,
          modifier = Modifier.size(48.dp),
          tint = Color.Unspecified
        )
      }

      Spacer(modifier = Modifier.size(8.dp))

      Text(
        text = name,
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 16.sp
        ),
        color = ExtendedTheme.colors.titleText,
        textAlign = TextAlign.Center,
        maxLines = 1
      )
    }
  }
}

class PreviewMainDishComponent : MainDishComponent {
  override val uiState = MutableStateFlow(PreviewData.mainDishState)
  override fun onRetry() = Unit
  override fun onDishCategoryClicked(dishCategory: DishCategory) = Unit
}

@UniversalScreenPreview
@Composable
private fun PreviewMainDishContent() = PreviewWrapper {
  MainDishContent(component = PreviewMainDishComponent(), extraContentPaddingBottom = 0.dp)
}
