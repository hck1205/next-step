package com.nextstep.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.domain.EventOccurrence
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.StatTile
import com.nextstep.app.ui.components.SubjectTag
import com.nextstep.app.ui.components.subjectColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    onOpenTimer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSubject: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("안녕, ${state.displayName.ifBlank { "학생" }}!", style = MaterialTheme.typography.titleLarge)
                        Text(DateUtils.formatFullDate(DateUtils.today()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = { IconButton(onClick = onOpenSettings) { Icon(Icons.Default.Settings, contentDescription = "설정") } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("오늘 공부", DateUtils.formatMinutes(state.todayMinutes), Modifier.weight(1f), icon = Icons.Default.Timer)
                    StatTile(
                        "이번 주", DateUtils.formatMinutes(state.weekMinutes), Modifier.weight(1f),
                        tint = MaterialTheme.colorScheme.secondary,
                        sub = if (state.weekGoalMinutes > 0) "목표 ${DateUtils.formatMinutes(state.weekGoalMinutes)}" else null,
                    )
                    StatTile(
                        "할 일", "${state.pendingTasks.size}개", Modifier.weight(1f),
                        tint = if (state.pendingTasks.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                        icon = Icons.Default.CheckCircle,
                    )
                }
            }
            item { TimerCard(state, onOpenTimer) }

            state.nextExam?.let { exam ->
                item {
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("다가오는 시험", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(exam.title, style = MaterialTheme.typography.titleMedium)
                                Text(DateUtils.formatFullDate(exam.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(DateUtils.dDay(exam.date), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { SectionTitle("오늘 일정") }
            if (state.todayEvents.isEmpty()) item { AppCard { EmptyState("오늘은 등록된 일정이 없어요") } }
            else items(state.todayEvents, key = { "ev" + it.event.id + it.startAt }) { occ -> EventRow(occ, state.subjects) }

            item { SectionTitle("오늘 할 일") }
            if (state.pendingTasks.isEmpty()) item { AppCard { EmptyState("할 일을 모두 끝냈어요 🎉") } }
            else items(state.pendingTasks, key = { "task" + it.id }) { task -> TaskRow(task, state.subjects, onToggle = { viewModel.toggleTask(task) }) }

            if (state.reviewQueue.isNotEmpty()) {
                item { SectionTitle("복습할 단원") }
                items(state.reviewQueue, key = { "rv" + it.second.id }) { (subject, topic) ->
                    TopicSuggestionRow(subject, topic, actionLabel = "복습 완료",
                        onAction = { viewModel.markTopic(topic, TopicStatus.REVIEWED) },
                        onAddTask = { viewModel.addQuickTask(subject, topic, TaskType.REVIEW) },
                        onOpen = { onOpenSubject(subject.id) })
                }
            }
            if (state.previewQueue.isNotEmpty()) {
                item { SectionTitle("예습할 단원") }
                items(state.previewQueue, key = { "pv" + it.second.id }) { (subject, topic) ->
                    TopicSuggestionRow(subject, topic, actionLabel = "예습 완료",
                        onAction = { viewModel.markTopic(topic, TopicStatus.PREVIEWED) },
                        onAddTask = { viewModel.addQuickTask(subject, topic, TaskType.PREVIEW) },
                        onOpen = { onOpenSubject(subject.id) })
                }
            }

            if (state.progress.isNotEmpty()) {
                item { SectionTitle("과목별 진도") }
                item {
                    AppCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.progress.forEach { p ->
                                LabeledProgress(
                                    label = "${p.subject.name}  (학급 ${p.classCovered}/${p.total})",
                                    ratio = p.myRatio,
                                    color = subjectColor(p.subject.color),
                                    trailing = "복습 ${p.reviewed}/${p.total}",
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun TimerCard(state: HomeUiState, onOpenTimer: () -> Unit) {
    val running = state.runningTimer
    AppCard(onClick = onOpenTimer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (running != null) {
                    val subject = state.subjects.firstOrNull { it.id == running.subjectId }
                    Text("공부 중 · ${subject?.name ?: "과목 없음"}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("${DateUtils.formatTime(running.startedAt)}부터 기록 중", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("학습 타이머", style = MaterialTheme.typography.titleMedium)
                    Text("공부를 시작할 때 눌러서 시간을 기록하세요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(onClick = onOpenTimer) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(if (running != null) "열기" else "시작")
            }
        }
    }
}

@Composable
fun EventRow(occ: EventOccurrence, subjects: List<SubjectEntity>) {
    val subject = subjects.firstOrNull { it.id == occ.event.subjectId }
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(56.dp)) {
                Text(DateUtils.formatTime(occ.startAt), style = MaterialTheme.typography.titleSmall)
                Text(DateUtils.formatTime(occ.endAt), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(occ.event.title, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(occ.event.type.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (subject != null) SubjectTag(subject)
                    if (occ.event.location.isNotBlank()) Text(occ.event.location, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun TaskRow(task: TaskEntity, subjects: List<SubjectEntity>, onToggle: () -> Unit, onDelete: (() -> Unit)? = null) {
    val subject = subjects.firstOrNull { it.id == task.subjectId }
    val overdue = !task.done && task.dueDate < DateUtils.today().toEpochDay()
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f)) {
                Text(
                    task.title, style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.done) TextDecoration.LineThrough else null,
                    color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(task.type.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (subject != null) SubjectTag(subject)
                    Text(
                        (if (overdue) "기한 지남 · " else "") + DateUtils.formatDate(DateUtils.fromEpochDay(task.dueDate)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (onDelete != null) TextButton(onClick = onDelete) { Text("삭제") }
        }
    }
}

@Composable
private fun TopicSuggestionRow(subject: SubjectEntity, topic: TopicEntity, actionLabel: String, onAction: () -> Unit, onAddTask: () -> Unit, onOpen: () -> Unit) {
    AppCard(onClick = onOpen) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubjectTag(subject)
                Spacer(Modifier.width(8.dp))
                Text(topic.title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onAddTask) { Text("할 일로 추가") }
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}
