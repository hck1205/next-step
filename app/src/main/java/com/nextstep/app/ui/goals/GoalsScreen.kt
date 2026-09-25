package com.nextstep.app.ui.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.goals.components.AddGoalDialog
import com.nextstep.app.ui.goals.components.AddMissionDialog
import com.nextstep.app.ui.goals.components.MissionCard
import com.nextstep.app.ui.goals.components.GoalCard
import com.nextstep.app.ui.goals.components.TrackCard
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * 목표 화면. 장기 목표를 구간(학기)별 단계로 쪼개 하나씩 진행합니다.
 * 트랙을 시작하거나 직접 목표를 만들고, 이번 구간의 단계를 완료하거나 할 일로 보냅니다.
 */
@Composable
fun GoalsScreen(caps: Capabilities, actions: GoalsActions, viewModel: GoalsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    GoalsContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalsContent(state: GoalsUiState, caps: Capabilities, actions: GoalsActions, onEvent: (GoalsEvent) -> Unit) {
    var showAdd by remember { mutableStateOf(false) }
    var showMission by remember { mutableStateOf(false) }
    var expandedGoalId by remember { mutableStateOf<String?>(null) }
    val goalCard: @Composable (GoalView) -> Unit = { view ->
        GoalCard(
            view = view, periodLabel = state::periodLabel, currentPeriodKey = state.currentPeriodKey,
            expanded = expandedGoalId == view.goal.id, onToggle = { expandedGoalId = if (expandedGoalId == view.goal.id) null else view.goal.id },
            canManage = caps.canManageGoals,
            onSetStepStatus = { step, status -> onEvent(GoalsEvent.SetStepStatus(step, status)) },
            onSendToTasks = { onEvent(GoalsEvent.SendStepToTasks(it, caps.actingRoleName)) },
            onAddStep = { periodKey, title -> onEvent(GoalsEvent.AddStep(view.goal.id, periodKey, title)) },
            onSetGoalStatus = { onEvent(GoalsEvent.SetGoalStatus(view.goal.id, it)) },
            onDelete = { onEvent(GoalsEvent.DeleteGoal(view.goal.id)) },
        )
    }

    Scaffold(
        // onBack 이 없으면 기록 탭의 섹션으로 들어간 것: 관심사·섹션 줄이 제목을 대신합니다.
        topBar = {
            if (actions.onBack != null) TopAppBar(
                title = { Text(if (state.studentName.isBlank()) "목표" else "${state.studentName}의 목표") },
                navigationIcon = { if (actions.onBack != null) IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
                actions = { TextButton(onClick = actions.onOpenJourney) { Text("타임라인") } },
            )
        },
        floatingActionButton = {
            if (caps.canManageGoals && state.hasBirthDate) FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, contentDescription = "목표 추가") }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!state.hasBirthDate) {
                item { AppCard { EmptyState("타임라인에서 생년월일을 먼저 입력하면 학기별 단계를 만들 수 있어요") } }
                return@LazyColumn
            }
            item {
                AppCard {
                    Text(
                        state.currentPeriodLabel?.let { "지금은 $it. 목표는 한 번에 이룰 수 없으니 이 구간의 단계 하나에만 집중해요." } ?: "목표를 구간별 단계로 나눠 하나씩 진행해요.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (state.missionKinds.isNotEmpty() || state.missions.isNotEmpty()) {
                item { SectionTitle("시험·입시", action = if (caps.canManageGoals && state.missionKinds.isNotEmpty()) ({ TextButton(onClick = { showMission = true }) { Text("추가") } }) else null) }
                if (state.missions.isEmpty()) item { AppCard { EmptyState("시험·수행평가 날짜를 넣으면 단계가 자동으로 나뉘어요") } }
                items(state.missions, key = { "m-" + it.goal.id }) { view ->
                    MissionCard(
                        view = view, today = state.today, expanded = expandedGoalId == view.goal.id,
                        onToggle = { expandedGoalId = if (expandedGoalId == view.goal.id) null else view.goal.id },
                        canManage = caps.canManageGoals,
                        onSetStepStatus = { step, status -> onEvent(GoalsEvent.SetStepStatus(step, status)) },
                        onSendToTasks = { onEvent(GoalsEvent.SendStepToTasks(it, caps.actingRoleName)) },
                        onDelete = { onEvent(GoalsEvent.DeleteGoal(view.goal.id)) },
                    )
                }
            }
            if (state.active.isNotEmpty()) item { SectionTitle("진행 중인 목표 · ${state.active.size}") }
            items(state.active, key = { it.goal.id }) { goalCard(it) }
            if (state.availableTracks.isNotEmpty()) {
                item { SectionTitle("시작할 수 있는 트랙 · ${state.availableTracks.size}") }
                items(state.availableTracks, key = { "t-${it.id}" }) { track ->
                    TrackCard(track = track, periodLabel = state::periodLabel, onStart = if (caps.canManageGoals) ({ onEvent(GoalsEvent.StartTrack(track.id)) }) else null)
                }
            }
            if (state.finished.isNotEmpty()) {
                item { SectionTitle("달성·보관 · ${state.finished.size}") }
                items(state.finished, key = { it.goal.id }) { goalCard(it) }
            }
        }
    }

    if (showMission && state.missionKinds.isNotEmpty()) AddMissionDialog(
        kinds = state.missionKinds, subjectNames = state.subjectNames, today = state.today,
        onConfirm = { kind, target, subject -> onEvent(GoalsEvent.StartMission(kind, target, subject, caps.actingRoleName)); showMission = false },
        onDismiss = { showMission = false },
    )
    if (showAdd) AddGoalDialog(
        periods = state.periods, currentPeriodKey = state.currentPeriodKey,
        onConfirm = { title, area, desc, steps -> onEvent(GoalsEvent.AddCustomGoal(title, area, desc, steps)); showAdd = false },
        onDismiss = { showAdd = false },
    )
}
