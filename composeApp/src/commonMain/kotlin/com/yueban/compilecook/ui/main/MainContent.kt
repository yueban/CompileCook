package com.yueban.compilecook.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.yueban.compilecook.ui.main.MainComponent.Child.Dishes
import com.yueban.compilecook.ui.main.MainComponent.Child.Tips
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
            onRandomDishClicked = component::onRandomDishClicked,
            onAboutClicked = component::onAboutClicked,
          )
        }
      )
    },
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          selected = activeChild is Tips,
          onClick = { component.onTabSelected(MainComponent.MainTab.TIPS) },
          icon = { Icon(Icons.Default.Info, stringResource(Res.string.main_tab_tips)) },
          label = { Text(stringResource(Res.string.main_tab_tips)) }
        )
        NavigationBarItem(
          selected = activeChild is Dishes,
          onClick = { component.onTabSelected(MainComponent.MainTab.DISHES) },
          icon = { Icon(Icons.Default.Fastfood, stringResource(Res.string.main_tab_dishes)) },
          label = { Text(stringResource(Res.string.main_tab_dishes)) }
        )
      }
    }
  ) { padding ->
    Children(
      stack = component.stack,
      modifier = Modifier.padding(padding),
      animation = stackAnimation(fade())
    ) {
      when (val child = it.instance) {
        is Dishes -> MainDishContent(child.component)
        is Tips -> MainTipContent(child.component)
      }
    }
  }
}

@Composable
private fun TopBarActions(
  activeChild: MainComponent.Child,
  onDishSearchClicked: () -> Unit,
  onRandomDishClicked: () -> Unit,
  onAboutClicked: () -> Unit,
) {
  if (activeChild is Dishes) {
    IconButton(onClick = onDishSearchClicked) {
      Icon(Icons.Default.Search, contentDescription = stringResource(Res.string.main_dish_des_search))
    }
  }

  IconButton(onClick = onRandomDishClicked) {
    Icon(
      imageVector = Icons.Outlined.Casino,
      contentDescription = stringResource(Res.string.main_des_random_dish)
    )
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
