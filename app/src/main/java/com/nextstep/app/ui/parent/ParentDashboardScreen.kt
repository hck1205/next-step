package com.nextstep.app.ui.parent

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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.BarChart
import com.nextstep.app.ui.components.BarItem
import com.nextstep.app.ui.components.DateField
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.components.OptionPicker
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.StatTile
import com.nextstep.app.ui.components.SubjectPicker
import com.nextstep.app.ui.components.SubjectTag
import com.nextstep.app.ui.components.SyncStatusBadge
import com.nextstep.app.ui.components.subjectColor
import com.nextstep.app.ui.insights.InsightCard
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onOpenSettings: () -> Unit,
    onOpenSubject: (String) -> Unit,
    onOpenInsights: () -> Unit,
    viewModel: ParentDashboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showNote by remember { mutableStateOf(false) }
    var showAssign by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${state.studentName.ifBlank { "자녀" }}의 학습 현황", style = MaterialTheme.typography.titleLarge)
                        SyncStatusBadge(state.syncStatus)
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
                    StatTile("오늘 학습", DateUtils.formatMinutes(state.todayMinutes), Modifier.weight(1f))
                    StatTile(
                        "이번 주", DateUtils.formatMinutes(state.weekMinutes), Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary,
                        sub = if (state.weekGoalMinutes > 0) "목표 대비 ${(state.weekMinutes * 100 / state.weekGoalMinutes)}%" else null,
                    )
                    StatTile(
                        "미완료", "${state.pendingTasks.size}개", Modifier.weight(1f),
                        tint = if (state.overdueCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        sub = if (state.overdueCount > 0) "기한 지남 ${state.overdueCount}" else null,
                    )
                }
            }

            item {
                SectionTitle("최근 7일 학습 시간")
                AppCard {
                    BarChart(
                        items = state.daily.map { d ->
                            BarItem(DateUtils.dayOfWeekLabel(d.date.dayOfWeek), d.minutes.toFloat(), if (d.date == DateUtils.today()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
                        },
                        valueFormatter = { DateUtils.formatMinutes(it.toInt()) },
                        height = 150,
                    )
                }
            }

            if (state.weeklyBySubject.isNotEmpty()) {
                item {
                    SectionTitle("이번 주 과목별 시간 (선: 목표)")
                    AppCard {
                        BarChart(
                            items = state.weeklyBySubject.map { w ->
                                BarItem(w.subject?.name ?: "기타", w.minutes.toFloat(), w.subject?.let { subjectColor(it.color) } ?: MaterialTheme.colorScheme.onSurfaceVariant, goal = w.goalMinutes.toFloat().takeIf { it > 0 })
                            },
                            valueFormatter = { DateUtils.formatMinutes(it.toInt()) },
                        )
                    }
                }
            }

            if (state.upcomingExams.isNotEmpty()) {
                item { SectionTitle("다가오는 시험") }
                items(state.upcomingExams, key = { it.title + it.date }) { exam ->
                    val subject = state.subjects.firstOrNull { it.id == exam.subjectId }
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(exam.title, style = MaterialTheme.typography.titleMedium)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (subject != null) SubjectTag(subject)
                                    Text(DateUtils.formatFullDate(exam.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(DateUtils.dDay(exam.date), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { SectionTitle("분석 요약", action = { TextButton(onClick = onOpenInsights) { Text("전체 보기") } }) }
            items(state.insights) { InsightCard(it, state.subjects, onAction = null) }

            if (state.progress.isNotEmpty()) {
                item { SectionTitle("과목별 진도 · 학급 진도 대비 복습률") }
                item {
                    AppCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.progress.forEach { p ->
                                LabeledProgress(
                                    label = "${p.subject.name}  (학급 ${p.classCovered}/${p.total} 단원)",
                                    ratio = if (p.classCovered == 0) 0f else p.reviewed.toFloat() / p.classCovered,
                                    color = subjectColor(p.subject.color),
                                    trailing = "복습 ${p.reviewed}/${p.classCovered}",
                                )
                            }
                        }
                    }
                }
            }

            if (state.scores.isNotEmpty()) {
                item { SectionTitle("과목별 성적 평균") }
                item {
                    AppCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.scores.sortedByDescending { it.average }.forEach { s ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SubjectTag(s.subject, Modifier.width(80.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("${String.format(java.util.Locale.ROOT, "%.1f", s.average)}점", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    s.trend?.let { t ->
                                        Text(
                                            (if (t >= 0) "▲ " else "▼ ") + String.format(java.util.Locale.ROOT, "%.1f", kotlin.math.abs(t)),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (t >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { SectionTitle("자녀에게 할 일 배정", action = { TextButton(onClick = { showAssign = true }) { Text("추가") } }) }
            if (state.pendingTasks.isEmpty()) item { AppCard { EmptyState("미완료 할 일이 없어요") } }
            else items(state.pendingTasks.take(5), key = { "t" + it.id }) { t ->
                val subject = state.subjects.firstOrNull { it.id == t.subjectId }
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t.title, style = MaterialTheme.typography.bodyLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(t.type.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                if (subject != null) SubjectTag(subject)
                                Text(DateUtils.formatDate(DateUtils.fromEpochDay(t.dueDate)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (t.createdByRole == "PARENT") Text("학부모 배정", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }

            item { SectionTitle("메모 · 응원", action = { TextButton(onClick = { showNote = true }) { Text("남기기") } }) }
            if (state.notes.isEmpty()) item { AppCard { EmptyState("자녀에게 응원 메모를 남겨 보세요") } }
            else items(state.notes, key = { "n" + it.id }) { n ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(n.text, style = MaterialTheme.typography.bodyLarge)
                            Text("${n.authorName} · ${DateUtils.formatDate(DateUtils.toLocalDate(n.createdAt))} ${DateUtils.formatTime(n.createdAt)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (n.authorRole == "PARENT") TextButton(onClick = { viewModel.deleteNote(n.id) }) { Text("삭제") }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showNote) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNote = false },
            title = { Text("메모 남기기") },
            text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("내용") }, modifier = Modifier.fillMaxWidth(), minLines = 2) },
            confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { viewModel.addNote(text); showNote = false }) { Text("저장") } },
            dismissButton = { TextButton(onClick = { showNote = false }) { Text("취소") } },
        )
    }
    if (showAssign) {
        AssignTaskDialog(state.subjects, onDismiss = { showAssign = false }) { title, subjectId, type, due ->
            viewModel.assignTask(title, subjectId, type, due)
        }
    }
}

@Composable
private fun AssignTaskDialog(subjects: List<SubjectEntity>, onDismiss: () -> Unit, onSave: (String, String?, TaskType, LocalDate) -> Unit) {
    var title by remember { mutableStateOf("") }
    var subjectId by remember { mutableStateOf<String?>(null) }
    var type by remember { mutableStateOf(TaskType.HOMEWORK) }
    var due by remember { mutableStateOf(LocalDate.now()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("할 일 배정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("할 일") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                SubjectPicker(subjects, subjectId, onSelect = { subjectId = it })
                OptionPicker(TaskType.entries, type, label = { it.label }, onSelect = { type = it })
                DateField("마감", due, onChange = { due = it })
            }
        },
        confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onSave(title.trim(), subjectId, type, due); onDismiss() }) { Text("배정") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
