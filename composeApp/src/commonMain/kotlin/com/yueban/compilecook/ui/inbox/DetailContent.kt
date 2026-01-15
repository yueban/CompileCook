package com.yueban.compilecook.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yueban.compilecook.ui.base.AsyncContent
import com.yueban.compilecook.ui.widget.EmptyComposable
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.common_des_back
import compilecook.composeapp.generated.resources.dish_detail_empty
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetailContent(component: DetailComponent, modifier: Modifier = Modifier) {
  val state by component.uiState.collectAsStateWithLifecycle()

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(state.dishAsync.invoke()?.name ?: "") },
        navigationIcon = {
          IconButton(onClick = component::onBackClicked) {
            Icon(
              imageVector = Icons.AutoMirrored.Default.ArrowBack,
              contentDescription = stringResource(Res.string.common_des_back)
            )
          }
        }
      )
    }
  ) { padding ->
    AsyncContent(
      async = state.dishAsync,
      modifier = Modifier.padding(padding),
      emptyContent = { EmptyComposable(stringResource(Res.string.dish_detail_empty)) }
    ) { dish ->
      Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = dish.name,
          style = MaterialTheme.typography.headlineLarge
        )
        Text(
          text = dish.description,
          style = MaterialTheme.typography.bodyLarge
        )
      }
    }
  }
}
