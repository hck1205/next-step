package com.nextstep.app.ui.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LinkCard
import com.nextstep.app.ui.components.card.UpcomingExamCard
import com.nextstep.app.ui.components.dialog.TextInputDialog
import com.nextstep.app.ui.parent.components.CheerPromptCard
import com.nextstep.app.ui.parent.components.PendingTaskRow
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.row.EventRow
import com.nextstep.app.ui.components.card.InsightCard
import com.nextstep.app.ui.components.card.JourneyNowCard
import com.nextstep.app.ui.components.card.MissionFocusCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.card.StageCard
import com.nextstep.app.ui.components.card.StatusCard
import com.nextstep.app.ui.components.card.StatusTile
import com.nextstep.app.ui.components.card.SyncStatusBadge
import com.nextstep.app.ui.components.card.TalentCard
import com.nextstep.app.ui.records.RecordSegment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.nextstep.app.ui.common.UiDefaults

/**
 * 학부모의 "오늘": 지금 뭐 하면 되지? 에만 답합니다.
 * 상태 문장 → 지금 챙길 것 → 오늘의 아이 → 격려. 그래프·성적·진도는 기록 탭에 있습니다.
 */
@Composable
fun ParentDashboardScreen(caps: Capabilities, actions: ParentDashboardActions, viewModel: ParentDashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ParentDashboardContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParentDashboardContent(state: ParentDashboardUiState, caps: Capabilities, actions: ParentDashboardActions, onEvent: (ParentDashboardEvent) -> Unit) {
    var showNote by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("오늘", style = MaterialTheme.typography.titleLarge)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(DateUtils.formatFullDate(state.today), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            SyncStatusBadge(state.syncStatus)
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                val b = state.balance
                StatusCard(
                    context = "${state.studentName.ifBlank { "자녀" }} · ${state.statusContext}",
                    headline = state.statusHeadline,
                    tiles = listOfNotNull(
                        StatusTile("이번 주 학습", DateUtils.formatMinutes(state.weekMinutes)),
                        b?.let { StatusTile("균형", it.studyVerdict.label) },
                        b?.selfDirectedRatio?.let { StatusTile("스스로", "${(it * 100).toInt()}%") } ?: StatusTile("연속", "${state.streak}일"),
                    ),
                )
            }
            if (caps.actsAsMentor) {
                item { LinkCard("멘토 모드", "로드맵 큐레이팅, 과제 배정, 학급 진도 관리는 여기서 해요", onClick = actions.onOpenMentor, titleColor = MaterialTheme.colorScheme.tertiary) }
            }

            item { SectionTitle("지금 챙길 것", action = { TextButton(onClick = actions.onOpenJourney) { Text("여정 전체") } }) }
            if (state.missionFocus.isNotEmpty()) item { MissionFocusCard(state.missionFocus, onOpen = actions.onOpenGoals) }
            item { JourneyNowCard(items = state.journeyNow, today = state.today, hasBirthDate = state.hasBirthDate, onOpen = actions.onOpenJourney) }
            item {
                StageCard(
                    stage = state.stage, gradeLabel = state.gradeLabel,
                    headline = state.stage?.let { "지금 해 줄 일" }, body = state.stageTip, experience = state.stageExperience,
                    onSetGrade = actions.onOpenSettings,
                )
            }

            item { SectionTitle("오늘의 ${state.studentName.ifBlank { "아이" }}", action = { TextButton(onClick = { actions.onOpenRecords(RecordSegment.CALENDAR) }) { Text("일정 전체") } }) }
            if (state.pendingTasks.isEmpty() && state.todayEvents.isEmpty()) item { AppCard { EmptyState("오늘은 잡힌 할 일과 일정이 없어요") } }
            items(state.pendingTasks.take(UiDefaults.MAX_ROWS), key = { "t" + it.id }) { t -> PendingTaskRow(t, state.subjects.firstOrNull { it.id == t.subjectId }) }
            if (state.pendingTasks.size > UiDefaults.MAX_ROWS) item {
                TextButton(onClick = { actions.onOpenRecords(RecordSegment.CALENDAR) }) { Text("할 일 ${state.pendingTasks.size - UiDefaults.MAX_ROWS}개 더 보기") }
            }
            items(state.todayEvents.take(UiDefaults.MAX_ROWS), key = { "ev" + it.event.id + it.startAt }) { occ -> EventRow(occ, state.subjects) }
            state.upcomingExams.firstOrNull()?.let { exam -> item { UpcomingExamCard(exam) } }

            item { CheerPromptCard(state.notes.firstOrNull()?.text, onOpen = actions.onOpenCheer, onWrite = { showNote = true }) }

            state.talents.firstOrNull()?.let { talent ->
                item { SectionTitle("재능 신호", action = { TextButton(onClick = { actions.onOpenRecords(RecordSegment.LEARNING) }) { Text("기록에서 더 보기") } }) }
                item { TalentCard(talent, state.subjects) }
            }
            state.insights.firstOrNull()?.let { insight ->
                item { SectionTitle("분석 한 줄", action = { TextButton(onClick = { actions.onOpenRecords(RecordSegment.BALANCE) }) { Text("균형 보기") } }) }
                item { InsightCard(insight, state.subjects, onAction = null) }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showNote) {
        TextInputDialog(title = "격려 한마디", label = "내용", minLines = 2, onConfirm = { onEvent(ParentDashboardEvent.AddNote(it)) }, onDismiss = { showNote = false })
    }
}
