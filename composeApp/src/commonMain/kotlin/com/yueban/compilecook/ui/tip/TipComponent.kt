package com.yueban.compilecook.ui.tip

import com.arkivanov.decompose.ComponentContext
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdownFlow
import com.yueban.compilecook.repo.DishRepo
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.Success
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.ui.base.Uninitialized
import com.yueban.compilecook.ui.tip.TipComponent.Output.BackClicked
import com.yueban.compilecook.ui.tip.TipComponent.Output.ImageClicked
import com.yueban.compilecook.ui.widget.markdown.TocItem
import com.yueban.compilecook.ui.widget.markdown.extractToc
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class TipState(
  val tipName: String,
  @Transient
  val contentAsync: Async<State> = Uninitialized,
  @Transient
  val tocAsync: Async<List<TocItem>> = Uninitialized,
)

interface TipComponent : UiStateComponent<TipState> {
  fun onBackClicked()
  fun onImageClicked(imageUrl: String)

  sealed interface Output {
    data object BackClicked : Output, BackOutput
    data class ImageClicked(val tipName: String, val imageUrl: String) : Output
  }
}

class DefaultTipComponent(
  componentContext: ComponentContext,
  private val tipName: String,
  private val onOutput: (TipComponent.Output) -> Unit,
  dishRepo: DishRepo,
) : TipComponent, UiStateComponentImpl<TipState>(
  componentContext = componentContext,
  initialState = TipState(tipName = tipName),
  serializer = TipState.serializer(),
) {
  override fun onBackClicked() = onOutput(BackClicked)

  override fun onImageClicked(imageUrl: String) = onOutput(ImageClicked(tipName, imageUrl))

  init {
    dishRepo.getTipByName(tipName)
      .filterNotNull()
      .map { it.content }
      .distinctUntilChanged()
      .flatMapLatest { parseMarkdownFlow(it) }
      .execute(retainValue = TipState::contentAsync) {
        copy(contentAsync = it)
      }

    uiState.mapNotNull { it.contentAsync as? Success }
      .mapNotNull { it.value as? State.Success }
      .map { extractToc(it) }
      .execute(retainValue = TipState::tocAsync) {
        copy(tocAsync = it)
      }
  }
}
