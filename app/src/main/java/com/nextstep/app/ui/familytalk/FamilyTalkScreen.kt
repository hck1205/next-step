package com.nextstep.app.ui.familytalk

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.familytalk.components.PastTalks
import com.nextstep.app.ui.familytalk.components.TalkPostcard
import com.nextstep.app.ui.familytalk.components.TalkSteps

@Composable
fun FamilyTalkScreen(caps: Capabilities, viewModel: FamilyTalkViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FamilyTalkContent(state = state, canTalk = caps.isFamily, onEvent = viewModel::onEvent)
}

/** 주말 이야기: 이미 나눴으면 "다음 주 카드" 엽서(다시 이야기하기), 아니면 네 걸음. 아래에 지난 이야기. */
@Composable
internal fun FamilyTalkContent(state: FamilyTalkUiState, canTalk: Boolean, onEvent: (FamilyTalkEvent) -> Unit) {
    if (!state.loaded) return
    var editing by rememberSaveable(state.week) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp).animateContentSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val saved = state.saved
        if (saved != null && state.talked && !editing) {
            TalkPostcard(saved, state.week, state.lookForward.firstOrNull(), state.today, onEdit = if (canTalk) ({ editing = true }) else null)
        } else if (canTalk) {
            TalkSteps(state, onSave = { proud, wish, treat -> onEvent(FamilyTalkEvent.Save(proud, wish, treat)); editing = false })
        } else {
            Text("주말 이야기는 가족이 함께 나눠요", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        PastTalks(state.past)
    }
}
