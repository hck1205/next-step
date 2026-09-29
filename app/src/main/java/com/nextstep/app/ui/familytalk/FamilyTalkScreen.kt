package com.nextstep.app.ui.familytalk

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
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.familytalk.components.TalkSteps
import com.nextstep.app.ui.familytalk.components.TalkSummaryCard

@Composable
fun FamilyTalkScreen(caps: Capabilities, viewModel: FamilyTalkViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FamilyTalkContent(state = state, canTalk = caps.isFamily, onEvent = viewModel::onEvent)
}

/** 주말 이야기: 이미 나눴으면 요약(다시 이야기하기), 아니면 네 단계. 머리에는 이야기하는 주. */
@Composable
internal fun FamilyTalkContent(state: FamilyTalkUiState, canTalk: Boolean, onEvent: (FamilyTalkEvent) -> Unit) {
    if (!state.loaded) return
    var editing by rememberSaveable(state.week) { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "${DateUtils.formatShortDate(state.week)} – ${DateUtils.formatShortDate(state.week.plusDays(LAST_DAY))} 주를 닫고, 다음 주를 열어요",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val saved = state.saved
        if (saved != null && state.talked && !editing) {
            TalkSummaryCard(saved, state.lookForward.firstOrNull(), onEdit = if (canTalk) ({ editing = true }) else null)
        } else if (canTalk) {
            TalkSteps(state, onSave = { proud, wish, treat -> onEvent(FamilyTalkEvent.Save(proud, wish, treat)); editing = false })
        }
    }
}

private const val LAST_DAY = 6L
