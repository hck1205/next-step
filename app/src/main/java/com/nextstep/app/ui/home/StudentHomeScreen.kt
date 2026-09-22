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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.nextstep.app.ui.components.TimeField
import java.time.LocalTime
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
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.PlanOptions
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
    onOpenRoadmap: () -> Unit,
    onOpenContent: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showPlanner by remember { mutableStateOf(false) }
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

            if (state.activeSubjects.isNotEmpty()) {
                item { SectionTitle("지금 배우는 과목") }
                item {
                    AppCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.activeSubjects.forEach { p ->
                                val current = p.reviewQueue.firstOrNull() ?: p.previewQueue.firstOrNull()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SubjectTag(p.subject)
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(current?.title ?: "다음 단원 없음", style = MaterialTheme.typography.bodyMedium)
                                        Text("학급 ${p.classCovered}/${p.total} 단원 · 복습 ${p.reviewed}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    TextButton(onClick = { onOpenSubject(p.subject.id) }) { Text("열기") }
                                }
                            }
                        }
                    }
                }
            }

            item {
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("학습 계획 만들기", style = MaterialTheme.typography.titleMedium)
                            Text("밀린 복습, 멘토 로드맵, 다음 예습을 빈 시간에 자동으로 배치해요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(onClick = { showPlanner = true }) { Text("계획") }
                    }
                }
            }

            if (state.roadmapFocus.isNotEmpty()) {
                item { SectionTitle("멘토 로드맵", action = { TextButton(onClick = onOpenRoadmap) { Text("전체 보기") } }) }
                items(state.roadmapFocus, key = { "rm" + it.id }) { r ->
                    val subject = state.subjects.firstOrNull { it.id == r.subjectId }
                    AppCard(onClick = onOpenRoadmap) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(r.title, style = MaterialTheme.typography.bodyLarge)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        if (subject != null) SubjectTag(subject)
                                        Text(r.status.label + (r.targetDate?.let { " · ${DateUtils.dDay(DateUtils.fromEpochDay(it))}" } ?: ""), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        if (r.createdByName.isNotBlank()) Text("${r.createdByName} 제안", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (r.status == RoadmapStatus.PLANNED) TextButton(onClick = { viewModel.setRoadmapStatus(r.id, RoadmapStatus.IN_PROGRESS) }) { Text("시작") }
                                else TextButton(onClick = { viewModel.setRoadmapStatus(r.id, RoadmapStatus.DONE) }) { Text("완료") }
                            }
                        }
                    }
                }
            }

            item { SectionTitle("추천 영상", action = { TextButton(onClick = onOpenContent) { Text("저장소") } }) }
            if (state.recommendations.isEmpty()) item {
                AppCard(onClick = onOpenContent) { Text("콘텐츠 저장소에 유튜브 링크를 등록하면 지금 배우는 단원에 맞는 영상을 골라 줘요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(state.recommendations, key = { "rec" + it.content.id }) { rec ->
                AppCard(onClick = { runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(rec.content.url))) } }) {
                    Column {
                        Text(rec.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(rec.content.title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(listOf(rec.content.channel, rec.content.subjectKey, rec.content.contentType.label).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.markContentWatched(rec.content.id) }) { Text("봤어요") }
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

    if (showPlanner) {
        PlannerDialog(onDismiss = { showPlanner = false }) { viewModel.generatePlan(it) }
    }
    state.lastPlan?.let { plan ->
        AlertDialog(
            onDismissRequest = viewModel::dismissPlanResult,
            title = { Text(if (plan.isEmpty) "배치할 항목이 없어요" else "학습 계획 완성") },
            text = {
                Text(
                    if (plan.isEmpty) "복습·예습할 단원이나 로드맵 항목이 없거나, 빈 시간이 없어요. 커리큘럼에서 단원과 학급 진도를 등록해 보세요."
                    else "${plan.events.size}개의 자습 일정과 할 일을 캘린더에 넣었어요. 첫 일정: ${DateUtils.formatDate(DateUtils.toLocalDate(plan.events.first().startAt))} ${DateUtils.formatTime(plan.events.first().startAt)} ${plan.events.first().title}",
                )
            },
            confirmButton = { TextButton(onClick = viewModel::dismissPlanResult) { Text("확인") } },
        )
    }
}

@Composable
private fun PlannerDialog(onDismiss: () -> Unit, onGenerate: (PlanOptions) -> Unit) {
    var days by remember { mutableStateOf("7") }
    var start by remember { mutableStateOf(LocalTime.of(19, 0)) }
    var minutes by remember { mutableStateOf("50") }
    var perDay by remember { mutableStateOf("2") }
    var weekend by remember { mutableStateOf(true) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("학습 계획 만들기") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("우선순위: 밀린 복습 → 멘토 로드맵 → 다음 예습. 이미 있는 일정과 겹치는 시간은 건너뛰어요.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = days, onValueChange = { days = it.filter { c -> c.isDigit() }.take(2) }, label = { Text("며칠") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                    OutlinedTextField(value = perDay, onValueChange = { perDay = it.filter { c -> c.isDigit() }.take(1) }, label = { Text("하루 회수") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TimeField("시작", start, onChange = { start = it }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = minutes, onValueChange = { minutes = it.filter { c -> c.isDigit() }.take(3) }, label = { Text("1회(분)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("주말 포함", modifier = Modifier.weight(1f))
                    Switch(checked = weekend, onCheckedChange = { weekend = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onGenerate(PlanOptions(days = (days.toIntOrNull() ?: 7).coerceIn(1, 30), startTime = start, sessionMinutes = (minutes.toIntOrNull() ?: 50).coerceIn(10, 180), sessionsPerDay = (perDay.toIntOrNull() ?: 2).coerceIn(1, 5), includeWeekend = weekend))
                onDismiss()
            }) { Text("만들기") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
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
                    if (task.createdByRole != "STUDENT") Text("${com.nextstep.app.data.model.Role.labelOf(task.createdByRole)} 배정", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
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
