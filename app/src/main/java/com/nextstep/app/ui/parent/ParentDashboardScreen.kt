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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.BarChart
import com.nextstep.app.ui.components.BarItem
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.InsightCard
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.StatTile
import com.nextstep.app.ui.components.SubjectTag
import com.nextstep.app.ui.components.SyncStatusBadge
import com.nextstep.app.ui.components.TalentCard
import com.nextstep.app.ui.components.subjectColor
import com.nextstep.app.ui.parent.components.AssignTaskDialog

@Composable
fun ParentDashboardScreen(caps: Capabilities, actions: ParentDashboardActions, viewModel: ParentDashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ParentDashboardContent(state = state, caps = caps, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParentDashboardContent(state: ParentDashboardUiState, caps: Capabilities, actions: ParentDashboardActions, onEvent: (ParentDashboardEvent) -> Unit) {
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
                actions = { IconButton(onClick = actions.onOpenSettings) { Icon(Icons.Default.Settings, contentDescription = "설정") } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (caps.actsAsMentor) {
                item {
                    AppCard(onClick = actions.onOpenMentor) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("멘토 모드", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.tertiary)
                                Text("로드맵 큐레이팅, 과제 배정, 학급 진도 관리는 여기서 해요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            TextButton(onClick = actions.onOpenRoadmap) { Text("로드맵") }
                            TextButton(onClick = actions.onOpenMentor) { Text("열기") }
                        }
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("연속 학습", "${state.streak}일", Modifier.weight(1f), tint = MaterialTheme.colorScheme.error)
                    StatTile("오늘 학습", DateUtils.formatMinutes(state.todayMinutes), Modifier.weight(1f))
                    StatTile(
                        "이번 주", DateUtils.formatMinutes(state.weekMinutes), Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary,
                        sub = if (state.weekGoalMinutes > 0) "목표 대비 ${(state.weekMinutes * 100 / state.weekGoalMinutes)}%" else null,
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        "미완료 할 일", "${state.pendingTasks.size}개", Modifier.weight(1f),
                        tint = if (state.overdueCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        sub = if (state.overdueCount > 0) "기한 지남 ${state.overdueCount}" else null,
                    )
                    StatTile("로드맵", if (state.roadmapTotal == 0) "-" else "${state.roadmapDone}/${state.roadmapTotal}", Modifier.weight(1f), tint = MaterialTheme.colorScheme.primary, sub = "완료 항목")
                    StatTile("연결", "멘토 ${state.mentorCount}", Modifier.weight(1f), tint = MaterialTheme.colorScheme.onSurfaceVariant, sub = "학부모 ${state.parentCount}")
                }
            }
            item {
                AppCard(onClick = actions.onOpenContent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("콘텐츠 저장소", style = MaterialTheme.typography.titleMedium)
                            Text("좋은 유튜브 강의를 등록해 두면 아이 진도에 맞춰 추천돼요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = actions.onOpenContent) { Text("열기") }
                    }
                }
            }
            if (state.talents.isNotEmpty()) {
                item { SectionTitle("재능 발견", action = { TextButton(onClick = actions.onOpenInsights) { Text("더 보기") } }) }
                items(state.talents) { TalentCard(it, state.subjects) }
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

            item { SectionTitle("분석 요약", action = { TextButton(onClick = actions.onOpenInsights) { Text("전체 보기") } }) }
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

            item {
                SectionTitle(
                    if (caps.canCreateTasks) "자녀 할 일 · 과제 배정" else "자녀 할 일",
                    action = if (caps.canCreateTasks) { { TextButton(onClick = { showAssign = true }) { Text("배정") } } } else null,
                )
            }
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
                                if (t.createdByRole != "STUDENT") Text("${com.nextstep.app.data.model.Role.labelOf(t.createdByRole)} 배정", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }

            item { SectionTitle("최근 메모 · 응원", action = { TextButton(onClick = { showNote = true }) { Text("남기기") } }) }
            if (state.notes.isEmpty()) item { AppCard { EmptyState("자녀에게 응원 메모를 남겨 보세요") } }
            else items(state.notes, key = { "n" + it.id }) { n ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(n.text, style = MaterialTheme.typography.bodyLarge)
                            Text("${n.authorName} · ${DateUtils.formatDate(DateUtils.toLocalDate(n.createdAt))} ${DateUtils.formatTime(n.createdAt)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(com.nextstep.app.data.model.Role.labelOf(n.authorRole), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (n.authorRole == "PARENT") TextButton(onClick = { onEvent(ParentDashboardEvent.DeleteNote(n.id)) }) { Text("삭제") }
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
            confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onEvent(ParentDashboardEvent.AddNote(text)); showNote = false }) { Text("저장") } },
            dismissButton = { TextButton(onClick = { showNote = false }) { Text("취소") } },
        )
    }
    if (showAssign) {
        AssignTaskDialog(state.subjects, onDismiss = { showAssign = false }) { title, subjectId, type, due ->
            onEvent(ParentDashboardEvent.AssignTask(title, subjectId, type, due, caps.actingRoleName))
        }
    }
}
