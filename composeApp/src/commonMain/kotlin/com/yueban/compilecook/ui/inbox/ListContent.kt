package com.yueban.compilecook.ui.inbox

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.logger.Logger
import com.yueban.compilecook.repo.entity.Dish
import com.yueban.compilecook.ui.base.AsyncListContent
import com.yueban.compilecook.ui.base.Fail
import com.yueban.compilecook.ui.base.Loading
import com.yueban.compilecook.ui.inbox.ListComponent.Event.BackFromDetail
import com.yueban.compilecook.ui.util.stringRes
import com.yueban.compilecook.ui.widget.EmptyComposable
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.dish_list_empty
import compilecook.composeapp.generated.resources.dish_list_item_difficulty
import compilecook.composeapp.generated.resources.dish_list_title
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun ListContent(component: ListComponent, modifier: Modifier = Modifier) {
  val state by component.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(component) {
    component.eventFlow.collect { event ->
      when (event) {
        is BackFromDetail -> Logger.d("Back From Detail: ${event.dishName}")
      }
    }
  }

  LaunchedEffect(state.loadingAsync) {
    (state.loadingAsync as? Fail)?.let {
      snackbarHostState.showSnackbar(
        message = getString(it.error.stringRes),
        withDismissAction = true
      )
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      Box {
        TopAppBar(title = { Text(stringResource(Res.string.dish_list_title)) })

        if (state.loadingAsync is Loading) {
          LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)
          )
        }
      }
    },
  ) { padding ->
    AsyncListContent(
      async = state.dishesAsync,
      modifier = modifier.padding(padding),
      onRetry = component::onRetry,
      emptyContent = { EmptyComposable(message = stringResource(Res.string.dish_list_empty)) }
    ) { dishes ->
      DishList(
        dishes = dishes,
        onItemClicked = component::onItemClicked
      )
    }
  }
}

@Composable
private fun DishList(
  dishes: List<Dish>,
  onItemClicked: (String) -> Unit,
) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items(dishes, key = { it.name }) { dish ->
      DishItem(dish = dish, onClick = { onItemClicked(dish.name) })
    }
  }
}

@Composable
private fun DishItem(
  dish: Dish,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = dish.name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = dish.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = dish.category.name,
          style = MaterialTheme.typography.labelSmall
        )
        Text(
          text = stringResource(Res.string.dish_list_item_difficulty, dish.difficulty),
          style = MaterialTheme.typography.labelSmall
        )
      }
    }
  }
}
