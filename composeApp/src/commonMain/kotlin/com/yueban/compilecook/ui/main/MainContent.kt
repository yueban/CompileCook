package com.yueban.compilecook.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.yueban.compilecook.ui.main.MainComponent.Child
import com.yueban.compilecook.ui.main.MainComponent.Child.Dishes
import com.yueban.compilecook.ui.main.MainComponent.Child.Tips
import com.yueban.compilecook.ui.main.MainComponent.MainTab
import com.yueban.compilecook.ui.util.PreviewWrapper
import com.yueban.compilecook.ui.util.UniversalScreenPreview
import com.yueban.compilecook.ui.widget.TitleTopBar
import compilecook.composeapp.generated.resources.Res
import compilecook.composeapp.generated.resources.app_name
import compilecook.composeapp.generated.resources.main_des_more
import compilecook.composeapp.generated.resources.main_des_random_dish
import compilecook.composeapp.generated.resources.main_dish_des_search
import compilecook.composeapp.generated.resources.main_menu_about
import compilecook.composeapp.generated.resources.main_tab_dishes
import compilecook.composeapp.generated.resources.main_tab_tips
import org.jetbrains.compose.resources.stringResource

private const val FAB_SPACER_WEIGHT = 0.5f
private val BOTTOM_FAB_OFFSET = 24.dp

@Composable
fun MainContent(component: MainComponent) {
  val stack by component.stack.subscribeAsState()
  val activeChild = stack.active.instance

  Scaffold(
    topBar = {
      TitleTopBar(
        title = stringResource(Res.string.app_name),
        actions = {
          TopBarActions(
            activeChild = activeChild,
            onDishSearchClicked = component::onDishSearchClicked,
            onAboutClicked = component::onAboutClicked,
          )
        }
      )
    },
    bottomBar = {
      ArchNavigationBar(
        activeChild = activeChild,
        onTabSelected = component::onTabSelected,
        onRandomDishClicked = component::onRandomDishClicked
      )
    }
  ) { padding ->
    Children(
      stack = component.stack,
      modifier = Modifier.padding(padding),
      animation = stackAnimation(fade())
    ) {
      when (val child = it.instance) {
        is Dishes -> MainDishContent(child.component, BOTTOM_FAB_OFFSET)
        is Tips -> MainTipContent(child.component, BOTTOM_FAB_OFFSET)
      }
    }
  }
}

@Composable
private fun ArchNavigationBar(
  activeChild: Child,
  onTabSelected: (MainTab) -> Unit,
  onRandomDishClicked: () -> Unit,
) {
  // unify container color and elevation to make a seamless shape.
  val barColor = NavigationBarDefaults.containerColor
  val barElevation = 3.dp

  Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.BottomCenter
  ) {
    NavigationBar(
      containerColor = barColor,
      tonalElevation = barElevation,
      modifier = Modifier.fillMaxWidth()
    ) {
      NavigationBarItem(
        selected = activeChild is Tips,
        onClick = { onTabSelected(MainTab.TIPS) },
        icon = { Icon(Icons.Default.Info, stringResource(Res.string.main_tab_tips)) },
        label = { Text(stringResource(Res.string.main_tab_tips)) }
      )

      // space for central fab
      Spacer(modifier = Modifier.weight(FAB_SPACER_WEIGHT))

      NavigationBarItem(
        selected = activeChild is Dishes,
        onClick = { onTabSelected(MainTab.DISHES) },
        icon = { Icon(Icons.Default.Fastfood, stringResource(Res.string.main_tab_dishes)) },
        label = { Text(stringResource(Res.string.main_tab_dishes)) }
      )
    }

    Surface(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .offset(y = -BOTTOM_FAB_OFFSET)
        .size(76.dp),
      shape = CircleShape,
      color = barColor,
      tonalElevation = barElevation,
      shadowElevation = 0.dp
    ) {
      Box(contentAlignment = Alignment.Center) {
        FloatingActionButton(
          onClick = onRandomDishClicked,
          shape = CircleShape,
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
          modifier = Modifier.size(56.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.Restaurant,
            contentDescription = stringResource(Res.string.main_des_random_dish),
            modifier = Modifier.size(28.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun TopBarActions(
  activeChild: Child,
  onDishSearchClicked: () -> Unit,
  onAboutClicked: () -> Unit,
) {
  if (activeChild is Dishes) {
    IconButton(onClick = onDishSearchClicked) {
      Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.main_dish_des_search))
    }
  }

  var showMenu by remember { mutableStateOf(false) }
  Box {
    IconButton(onClick = { showMenu = true }) {
      Icon(Icons.Default.MoreVert, contentDescription = stringResource(Res.string.main_des_more))
    }
    DropdownMenu(
      expanded = showMenu,
      onDismissRequest = { showMenu = false }
    ) {
      DropdownMenuItem(
        text = { Text(stringResource(Res.string.main_menu_about)) },
        onClick = {
          showMenu = false
          onAboutClicked()
        }
      )
    }
  }
}

private class PreviewMainComponent(activeTab: MainTab = MainTab.TIPS) : MainComponent {
  override val stack: Value<ChildStack<*, Child>> = MutableValue(
    ChildStack(
      configuration = activeTab.name,
      instance = when (activeTab) {
        MainTab.TIPS -> Tips(PreviewMainTipComponent())
        MainTab.DISHES -> Dishes(PreviewMainDishComponent())
      }
    )
  )

  override fun onTabSelected(tab: MainTab) = Unit
  override fun onDishSearchClicked() = Unit
  override fun onRandomDishClicked() = Unit
  override fun onAboutClicked() = Unit
}

@UniversalScreenPreview
@Composable
private fun PreviewMainContentTips() = PreviewWrapper {
  MainContent(component = PreviewMainComponent(activeTab = MainTab.TIPS))
}

@UniversalScreenPreview
@Composable
private fun PreviewMainContentDishes() = PreviewWrapper {
  MainContent(component = PreviewMainComponent(activeTab = MainTab.DISHES))
}
