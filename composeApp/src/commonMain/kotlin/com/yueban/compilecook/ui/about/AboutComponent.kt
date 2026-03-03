package com.yueban.compilecook.ui.about

import com.arkivanov.decompose.ComponentContext
import com.mikepenz.aboutlibraries.Libs
import com.yueban.compilecook.ui.about.AboutComponent.Output
import com.yueban.compilecook.ui.about.AboutComponent.Output.BackClicked
import com.yueban.compilecook.ui.base.Async
import com.yueban.compilecook.ui.base.BackOutput
import com.yueban.compilecook.ui.base.UiStateComponent
import com.yueban.compilecook.ui.base.UiStateComponentImpl
import com.yueban.compilecook.ui.base.Uninitialized
import compilecook.composeapp.generated.resources.Res
import kotlinx.serialization.Serializable

@Serializable
data class AboutState(
  val aboutLibs: Async<Libs?> = Uninitialized,
)

interface AboutComponent : UiStateComponent<AboutState> {
  fun onBackClicked()

  sealed interface Output {
    data object BackClicked : Output, BackOutput
  }
}

class DefaultAboutComponent(
  componentContext: ComponentContext,
  private val onOutput: (Output) -> Unit,
) : AboutComponent, UiStateComponentImpl<AboutState>(
  componentContext = componentContext,
  initialState = AboutState(),
  serializer = AboutState.serializer(),
) {
  init {
    suspend {
      Res.readBytes("files/aboutlibraries.json").decodeToString().let {
        Libs.Builder().withJson(it).build()
      }
    }.execute(retainValue = AboutState::aboutLibs) {
      copy(aboutLibs = it)
    }
  }

  override fun onBackClicked() = onOutput(BackClicked)
}
