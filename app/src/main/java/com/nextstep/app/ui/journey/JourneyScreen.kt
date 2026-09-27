package com.nextstep.app.ui.journey

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.CurriculumCard
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.layout.AppBarMenu
import com.nextstep.app.ui.components.layout.AppBarMenuItem
import com.nextstep.app.ui.components.layout.BackButton
import com.nextstep.app.ui.components.layout.ScreenPadding
import com.nextstep.app.ui.components.row.GoalStepRow
import com.nextstep.app.ui.journey.components.ActivityChips
import com.nextstep.app.ui.journey.components.CategoryFilter
import com.nextstep.app.ui.journey.components.JourneyDialog
import com.nextstep.app.ui.journey.components.JourneyDialogs
import com.nextstep.app.ui.journey.components.JourneyHeader
import com.nextstep.app.ui.journey.components.MilestoneCard
import com.nextstep.app.ui.journey.components.PeriodHeader
import com.nextstep.app.ui.journey.components.ToggleLine

/**
 * 구간(학기)별 여정 타임라인. 각 구간에 그 시기의 이정표와 목표 단계가 놓이고, 현재 구간이 강조됩니다.
 * 학부모·학생·멘토 누구나 완료 표시와 메모를 남길 수 있습니다.
 */
@Composable
fun JourneyScreen(caps: Capabilities, actions: JourneyActions, viewModel: JourneyViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    JourneyContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun JourneyContent(state: JourneyUiState, caps: Capabilities, actions: JourneyActions, onEvent: (JourneyEvent) -> Unit) {
    var expandedKey by remember { mutableStateOf<String?>(null) }
    var dialog by remember { mutableStateOf<JourneyDialog?>(null) }
    val milestone: @Composable (JourneyItem) -> Unit = { item ->
        MilestoneCard(item, state.today, expandedKey, { expandedKey = it }, onEvent, { dialog = JourneyDialog.Note(it) }, { dialog = JourneyDialog.DueDate(it) })
    }
    Scaffold(
        topBar = { JourneyTopBar(state.studentName, actions) },
        floatingActionButton = {
            if (caps.canEditJourney) FloatingActionButton(onClick = { dialog = JourneyDialog.Add }) { Icon(Icons.Default.Add, contentDescription = "이정표 추가") }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = ScreenPadding.list,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 분류 칩은 이 화면의 거르기라 상단 바 바로 아래에 붙여 둡니다(스크롤해도 남음). 그 아래 지금 나이 요약.
            if (state.hasBirthDate || state.items.isNotEmpty()) stickyHeader(key = "journey-filter") {
                Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
                    CategoryFilter(state.filter) { onEvent(JourneyEvent.SetFilter(it)) }
                }
            }
            item { JourneyHeader(state, onEvent) }
            if (state.loaded && state.items.isEmpty() && state.steps.isEmpty()) {
                item { EmptyCard(if (state.hasBirthDate) "표시할 이정표가 없어요" else "생년월일을 입력하면 나이대별 준비 항목이 자동으로 채워져요") }
            }
            if (state.hasBirthDate) periodSections(state, caps, actions, onEvent, milestone)
            else phaseSections(state, milestone)
            item { ToggleLine("완료·건너뛴 항목 보기", state.showCompleted) { onEvent(JourneyEvent.ShowCompleted(it)) } }
        }
    }
    JourneyDialogs(dialog, state.today, onEvent, onDismiss = { dialog = null })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun JourneyTopBar(studentName: String, actions: JourneyActions) {
    TopAppBar(
        title = { Text(if (studentName.isBlank()) "성장 여정" else "${studentName}의 성장 여정") },
        navigationIcon = { BackButton(actions.onBack) },
        actions = {
            // 자주 여는 "올해"만 보이게, 나머지 화면은 ⋮ 로. 설정은 하단 가족 탭이 맡습니다.
            actions.onOpenYear?.let { TextButton(onClick = it) { Text("올해") } }
            AppBarMenu(
                listOf(
                    AppBarMenuItem("목표", Icons.Default.Flag, actions.onOpenGoals),
                    AppBarMenuItem("활동 기록", Icons.Default.Star, actions.onOpenActivities),
                ),
            )
        },
    )
}

/** 생년월일이 있을 때: 나이 구간마다 제목 · (지금 구간이면) 이번 학기 커리큘럼 · 목표 단계 · 이정표 · 그때 한 활동. */
private fun LazyListScope.periodSections(
    state: JourneyUiState, caps: Capabilities, actions: JourneyActions, onEvent: (JourneyEvent) -> Unit, milestone: @Composable (JourneyItem) -> Unit,
) {
    if (state.pastSectionCount > 0) item { ToggleLine("지난 구간 ${state.pastSectionCount}개 보기", state.showPast) { onEvent(JourneyEvent.ShowPast(it)) } }
    state.periodSections.forEach { section ->
        item(key = "p-${section.period.key}") { PeriodHeader(section.period, section.isCurrent, section.isPast) }
        if (section.isCurrent) state.curriculum?.let { c -> item(key = "c-${section.period.key}") { CurriculumCard(curriculum = c, periodLabel = section.period.label, onOpen = actions.onOpenCurriculum) } }
        if (section.isCurrent && section.milestones.isEmpty() && section.steps.isEmpty()) {
            item { Text("이번 구간에 잡힌 항목이 없어요. 목표 화면에서 트랙을 시작해 보세요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        items(section.steps, key = { "s-${it.step.id}" }) { view ->
            AppCard {
                GoalStepRow(
                    step = view.step, goalTitle = view.goalTitle,
                    onSetStatus = { onEvent(JourneyEvent.SetStepStatus(view.step, it)) },
                    onSendToTasks = if (caps.canManageGoals) ({ onEvent(JourneyEvent.SendStepToTasks(view.step, caps.actingRoleName)) }) else null,
                )
            }
        }
        items(section.milestones, key = { milestoneKey(it) }) { milestone(it) }
        if (section.activities.isNotEmpty()) item(key = "a-${section.period.key}") { ActivityChips(section.activities, actions.onOpenActivities) }
    }
}

/** 생년월일이 없을 때: 단계(영유아·초등…)별 이정표. */
private fun LazyListScope.phaseSections(state: JourneyUiState, milestone: @Composable (JourneyItem) -> Unit) {
    state.phaseSections.forEach { (phase, group) ->
        item { SectionTitle("${phase.label} · ${group.size}") }
        items(group, key = { milestoneKey(it) }) { milestone(it) }
    }
}

private fun milestoneKey(item: JourneyItem): String = "m-${item.templateId ?: item.entityId ?: item.title}"
