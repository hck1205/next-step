package com.nextstep.app.ui.journey

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.MilestoneCategory
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.DateField
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.GoalStepRow
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.TextInputDialog
import com.nextstep.app.ui.journey.components.AddMilestoneDialog
import com.nextstep.app.ui.journey.components.MilestoneRow
import com.nextstep.app.ui.journey.components.PeriodHeader
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * 구간(학기)별 여정 타임라인. 각 구간에 그 시기의 이정표와 목표 단계가 놓이고, 현재 구간이 강조됩니다.
 * 학부모·학생·멘토 누구나 완료 표시와 메모를 남길 수 있습니다.
 */
@Composable
fun JourneyScreen(caps: Capabilities, actions: JourneyActions, viewModel: JourneyViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    JourneyContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun JourneyContent(state: JourneyUiState, caps: Capabilities, actions: JourneyActions, onEvent: (JourneyEvent) -> Unit) {
    var expandedKey by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var noteTarget by remember { mutableStateOf<JourneyItem?>(null) }
    var dateTarget by remember { mutableStateOf<JourneyItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.studentName.isBlank()) "성장 여정" else "${state.studentName}의 성장 여정") },
                navigationIcon = { if (actions.onBack != null) IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
                actions = {
                    TextButton(onClick = actions.onOpenGoals) { Text("목표") }
                    TextButton(onClick = actions.onOpenSettings) { Text("설정") }
                },
            )
        },
        floatingActionButton = {
            if (caps.canEditJourney) FloatingActionButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, contentDescription = "이정표 추가") }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { JourneyHeader(state, onEvent) }
            if (state.hasBirthDate || state.items.isNotEmpty()) {
                item { CategoryFilter(state.filter) { onEvent(JourneyEvent.SetFilter(it)) } }
            }
            if (state.loaded && state.items.isEmpty() && state.steps.isEmpty()) {
                item { AppCard { EmptyState(if (state.hasBirthDate) "표시할 이정표가 없어요" else "생년월일을 입력하면 나이대별 준비 항목이 자동으로 채워져요") } }
            }
            if (state.hasBirthDate) {
                if (state.pastSectionCount > 0) item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("지난 구간 ${state.pastSectionCount}개 보기", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Switch(checked = state.showPast, onCheckedChange = { onEvent(JourneyEvent.ShowPast(it)) })
                    }
                }
                state.periodSections.forEach { section ->
                    item(key = "p-${section.period.key}") { PeriodHeader(section.period, section.isCurrent, section.isPast) }
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
                    items(section.milestones, key = { "m-${it.templateId ?: it.entityId ?: it.title}" }) { item ->
                        MilestoneCard(item, state.today, expandedKey, { expandedKey = it }, onEvent, { noteTarget = it }, { dateTarget = it })
                    }
                }
            } else {
                state.phaseSections.forEach { (phase, group) ->
                    item { SectionTitle("${phase.label} · ${group.size}") }
                    items(group, key = { "m-${it.templateId ?: it.entityId ?: it.title}" }) { item ->
                        MilestoneCard(item, state.today, expandedKey, { expandedKey = it }, onEvent, { noteTarget = it }, { dateTarget = it })
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("완료·건너뛴 항목 보기", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Switch(checked = state.showCompleted, onCheckedChange = { onEvent(JourneyEvent.ShowCompleted(it)) })
                }
            }
        }
    }

    if (showAdd) AddMilestoneDialog(
        today = state.today,
        onConfirm = { title, desc, category, due, lead -> onEvent(JourneyEvent.AddCustom(title, desc, category, due, lead)); showAdd = false },
        onDismiss = { showAdd = false },
    )
    noteTarget?.let { target ->
        TextInputDialog(title = "메모", label = "예: 3월에 대기 신청 완료", initial = target.note, onConfirm = { onEvent(JourneyEvent.SetNote(target, it)); noteTarget = null }, onDismiss = { noteTarget = null })
    }
    dateTarget?.let { target ->
        DueDateDialog(target, onConfirm = { onEvent(JourneyEvent.SetDueDate(target, it)); dateTarget = null }, onDismiss = { dateTarget = null })
    }
}

@Composable
private fun MilestoneCard(
    item: JourneyItem, today: LocalDate, expandedKey: String?, setExpanded: (String?) -> Unit, onEvent: (JourneyEvent) -> Unit,
    onNote: (JourneyItem) -> Unit, onDate: (JourneyItem) -> Unit,
) {
    val key = item.templateId ?: item.entityId ?: item.title
    MilestoneRow(
        item = item, today = today, expanded = expandedKey == key,
        onToggleExpand = { setExpanded(if (expandedKey == key) null else key) },
        onSetStatus = { onEvent(JourneyEvent.SetStatus(item, it)) },
        onEditNote = { onNote(item) },
        onEditDate = { onDate(item) },
        onDelete = if (item.isCustom) ({ onEvent(JourneyEvent.DeleteCustom(item)) }) else null,
    )
}

@Composable
private fun JourneyHeader(state: JourneyUiState, onEvent: (JourneyEvent) -> Unit) {
    AppCard {
        Column {
            if (!state.hasBirthDate) {
                Text("생년월일로 여정을 시작해요", style = MaterialTheme.typography.titleMedium)
                Text("어린이집 대기, 예방접종, 유치원 지원, 언어 민감기, 학기별 목표, 입시 일정까지 나이에 맞춰 미리 알려 드려요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                DateField(label = "생년월일", date = state.today.minusYears(3), onChange = { onEvent(JourneyEvent.SetBirthDate(it)) })
            } else {
                Text("${state.ageLabel} · ${state.stage?.label ?: ""}", style = MaterialTheme.typography.titleMedium)
                state.stage?.let { Text(it.focus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Spacer(Modifier.height(8.dp))
                LabeledProgress(label = "지금까지의 여정", ratio = state.completion, color = MaterialTheme.colorScheme.primary, trailing = "${(state.completion * 100).toInt()}%")
                if (state.overdueCount > 0 || state.nowCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        buildString {
                            if (state.overdueCount > 0) append("지난 항목 ${state.overdueCount}개")
                            if (state.overdueCount > 0 && state.nowCount > 0) append(" · ")
                            if (state.nowCount > 0) append("지금 준비할 것 ${state.nowCount}개")
                        },
                        style = MaterialTheme.typography.bodySmall, color = if (state.overdueCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(selected: MilestoneCategory?, onSelect: (MilestoneCategory?) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("전체") })
        MilestoneCategory.entries.forEach { c ->
            FilterChip(selected = selected == c, onClick = { onSelect(if (selected == c) null else c) }, label = { Text(c.label) })
        }
    }
}

@Composable
private fun DueDateDialog(item: JourneyItem, onConfirm: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    var date by remember { mutableStateOf(item.dueDate) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("마감일 변경") },
        text = { DateField(label = "마감일", date = date, onChange = { date = it }) },
        confirmButton = { TextButton(onClick = { onConfirm(date) }) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
