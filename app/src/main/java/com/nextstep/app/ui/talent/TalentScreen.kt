package com.nextstep.app.ui.talent

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.talent.components.AptitudeCard
import com.nextstep.app.ui.talent.components.ObservationDialog
import com.nextstep.app.ui.talent.components.ObservationRow

/** 활동·재능 › 재능. 신호 카드 → 영역별 관찰 메모 전체(영역 머리는 위에 붙어 따라옵니다). */
@Composable
fun TalentScreen(caps: Capabilities, viewModel: TalentViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    TalentContent(state = state, caps = caps, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TalentContent(state: TalentUiState, caps: Capabilities, onEvent: (TalentEvent) -> Unit) {
    var showObserve by remember { mutableStateOf(false) }
    if (showObserve) ObservationDialog(today = state.today, onConfirm = { onEvent(TalentEvent.Observe(it)); showObserve = false }, onDismiss = { showObserve = false })

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { AptitudeCard(signals = state.signals, onObserve = if (caps.canRecordGrowth) ({ showObserve = true }) else null) }
        state.byDomain.forEach { (domain, observations) ->
            stickyHeader(key = "d${domain.name}") {
                Text(
                    "${domain.label} · ${observations.size}", style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(vertical = 6.dp),
                )
            }
            items(observations, key = { it.id }) { o ->
                ObservationRow(o, onDelete = if (caps.canRecordGrowth) ({ onEvent(TalentEvent.Delete(o.id)) }) else null)
            }
        }
    }
}
