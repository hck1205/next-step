package com.nextstep.app.ui.records

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.calendar.CalendarScreen
import com.nextstep.app.ui.components.input.SegmentedRow
import com.nextstep.app.ui.grades.GradesScreen
import com.nextstep.app.ui.insights.InsightsActions
import com.nextstep.app.ui.insights.InsightsScreen
import com.nextstep.app.ui.progress.ProgressActions
import com.nextstep.app.ui.progress.ProgressScreen
import com.nextstep.app.ui.records.components.BalanceContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * 기록 탭: "어떻게 하고 있지?" 에 답합니다. 세그먼트 하나가 화면 하나이고, 첫 번째는 항상 균형입니다.
 * 학습·성적·진도·일정 세그먼트는 각 기능 화면을 그대로 품습니다(각자 ViewModel).
 */
@Composable
fun RecordsScreen(
    caps: Capabilities,
    actions: RecordsActions,
    initialSegment: RecordSegment = RecordSegment.BALANCE,
    viewModel: RecordsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var segment by rememberSaveable(initialSegment) { mutableStateOf(initialSegment) }
    RecordsContent(state = state, caps = caps, actions = actions, segment = segment, onSegment = { segment = it }, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordsContent(state: RecordsUiState, caps: Capabilities, actions: RecordsActions, segment: RecordSegment, onSegment: (RecordSegment) -> Unit, onEvent: (RecordsEvent) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(if (caps.isStudent) "나" else "기록") }) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SegmentedRow(options = RecordSegment.entries, selected = segment, label = { it.label }, onSelect = onSegment, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp))
            Box(Modifier.fillMaxSize()) {
                when (segment) {
                    RecordSegment.BALANCE -> BalanceContent(state, caps, actions, onEvent)
                    RecordSegment.LEARNING -> InsightsScreen(caps = caps, actions = InsightsActions())
                    RecordSegment.GRADES -> GradesScreen(caps = caps)
                    RecordSegment.PROGRESS -> ProgressScreen(caps = caps, actions = ProgressActions(onOpenSubject = actions.onOpenSubject, onOpenRoadmap = actions.onOpenRoadmap))
                    RecordSegment.CALENDAR -> CalendarScreen(caps = caps)
                }
            }
        }
    }
}
