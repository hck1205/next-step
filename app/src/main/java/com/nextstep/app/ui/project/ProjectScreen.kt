package com.nextstep.app.ui.project

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.RoutineCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.chart.MinutesLadder
import com.nextstep.app.ui.components.dialog.ConfirmDialog
import com.nextstep.app.ui.project.components.CheckpointCard
import com.nextstep.app.ui.project.components.PhaseTimeline
import com.nextstep.app.ui.project.components.ProjectHeaderCard

/**
 * 교육 프로젝트 한 개. 위에서 아래로: 목표와 도착 예상 → 오늘 루틴 → 이 단계의 통과 기준 → 하루 양이 늘어나는 계단 → 단계 일정 → 왜 이 순서인지.
 */
@Composable
fun ProjectScreen(caps: Capabilities, actions: ProjectActions, viewModel: ProjectViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ProjectContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProjectContent(state: ProjectUiState, caps: Capabilities, actions: ProjectActions, onEvent: (ProjectEvent) -> Unit) {
    var confirmPass by remember { mutableStateOf(false) }
    var confirmArchive by remember { mutableStateOf(false) }
    val p = state.progress
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(p?.plan?.title ?: "교육 프로젝트") },
                navigationIcon = { IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (p == null) {
                if (state.loaded) item { AppCard { EmptyState("이 프로젝트를 찾을 수 없어요") } }
                return@LazyColumn
            }
            item { ProjectHeaderCard(p) }
            if (!p.isDone) {
                item { SectionTitle("오늘의 루틴") }
                item { RoutineCard(items = listOf(p), onToggle = { _, item -> onEvent(ProjectEvent.ToggleRoutine(item)) }, onOpen = {}) }
                p.current?.let { phase ->
                    item { CheckpointCard(phase, isLast = p.next == null, onPass = if (caps.canManageGoals) ({ confirmPass = true }) else null) }
                }
            }
            item { SectionTitle("하루 양은 이렇게 늘어요") }
            item {
                AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MinutesLadder(p.plan.phases, current = p.currentIndex.takeIf { !p.isDone }, skippedBefore = state.skipped)
                        Text("막대 위 숫자는 단계별 하루 평균(분)이에요", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item { SectionTitle("단계 일정") }
            item { PhaseTimeline(state.slots, currentIndex = p.currentIndex, skipped = state.skipped) }
            item { SectionTitle("왜 이 순서일까요") }
            item { AppCard { Text(p.plan.why, style = MaterialTheme.typography.bodyMedium) } }
            if (caps.canManageGoals && !p.isDone) item { TextButton(onClick = { confirmArchive = true }) { Text("이 프로젝트 그만두기") } }
        }
    }
    if (confirmPass && p?.current != null) {
        ConfirmDialog(
            title = "통과했어요?", text = "\"${p.current?.checkpoint}\"\n다음 단계의 루틴으로 바꿀게요.",
            confirmLabel = "통과", onConfirm = { onEvent(ProjectEvent.PassCheckpoint); confirmPass = false }, onDismiss = { confirmPass = false },
        )
    }
    if (confirmArchive) {
        ConfirmDialog(
            title = "그만둘까요?", text = "진행 중 목록에서 빠지고, 지금까지의 기록은 남아요.",
            confirmLabel = "그만두기", onConfirm = { onEvent(ProjectEvent.Archive); confirmArchive = false; actions.onBack() }, onDismiss = { confirmArchive = false },
        )
    }
}
