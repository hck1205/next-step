package com.nextstep.app.ui.mentor

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.BarChart
import com.nextstep.app.ui.components.BarItem
import com.nextstep.app.ui.components.ColorDot
import com.nextstep.app.ui.components.DateField
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.InsightCard
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.components.OptionPicker
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.StatTile
import com.nextstep.app.ui.components.SubjectPicker
import com.nextstep.app.ui.components.SubjectSelectDialog
import com.nextstep.app.ui.components.SubjectTag
import com.nextstep.app.ui.components.SyncStatusBadge
import com.nextstep.app.ui.components.subjectColor
import java.time.LocalDate
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun MentorDashboardScreen(actions: MentorDashboardActions, viewModel: MentorDashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MentorDashboardContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MentorDashboardContent(state: MentorDashboardUiState, actions: MentorDashboardActions, onEvent: (MentorDashboardEvent) -> Unit) {
    var showSubjects by remember { mutableStateOf(false) }
    var showAssign by remember { mutableStateOf(false) }
    var showNote by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("${state.studentName.ifBlank { "학생" }} · ${state.me?.title?.ifBlank { null } ?: Role.MENTOR.label}", style = MaterialTheme.typography.titleLarge)
                        SyncStatusBadge(state.syncStatus)
                    }
                },
                navigationIcon = { if (actions.onBack != null) IconButton(onClick = actions.onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
                actions = { IconButton(onClick = actions.onOpenSettings) { Icon(Icons.Default.Settings, contentDescription = "설정") } },
            )
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                AppCard(onClick = actions.onOpenRoadmap) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("학습 로드맵 큐레이팅", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (state.roadmapTotal == 0) "무엇을 어떤 순서로, 어떤 자료로, 언제까지 공부할지 제안해 보세요"
                                else "진행 중 ${state.roadmapInProgress} · 완료 ${state.roadmapDone}/${state.roadmapTotal}" + (if (state.roadmapOverdue > 0) " · 기한 지남 ${state.roadmapOverdue}" else ""),
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = actions.onOpenRoadmap) { Text("열기") }
                    }
                }
            }
            item {
                AppCard(onClick = actions.onOpenContent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("콘텐츠 저장소", style = MaterialTheme.typography.titleMedium)
                            Text("좋은 유튜브 강의를 링크로 등록하면 자동 분류되고 학생 진도에 맞춰 추천돼요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = actions.onOpenContent) { Text("열기") }
                    }
                }
            }
            item {
                AppCard(onClick = { showSubjects = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("담당 과목", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (state.needsSubjectSetup) {
                                Text("아직 지정하지 않았어요. 눌러서 담당 과목을 고르세요.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    state.subjects.take(4).forEach { SubjectTag(it) }
                                    if (state.subjects.size > 4) Text("+${state.subjects.size - 4}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        TextButton(onClick = { showSubjects = true }) { Text("변경") }
                    }
                }
            }

            if (state.otherMentors.isNotEmpty()) {
                item {
                    AppCard {
                        Text(
                            "함께 연결된 멘토: " + state.otherMentors.joinToString { m -> m.name + (if (m.title.isNotBlank()) " (${m.title})" else "") },
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("이번 주 학습", DateUtils.formatMinutes(state.weekMinutes), Modifier.weight(1f), sub = "담당 과목 기준")
                    StatTile("내가 낸 과제", "${state.myTasks.size}개", Modifier.weight(1f), tint = MaterialTheme.colorScheme.tertiary, sub = "미완료")
                    StatTile(
                        "평균 점수",
                        state.scores.takeIf { it.isNotEmpty() }?.let { String.format(Locale.ROOT, "%.1f", it.map { s -> s.average }.average()) } ?: "-",
                        Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            if (state.weeklyBySubject.isNotEmpty()) {
                item {
                    SectionTitle("이번 주 과목별 학습 시간 (선: 목표)")
                    AppCard {
                        BarChart(
                            items = state.weeklyBySubject.map { w ->
                                BarItem(w.subject!!.name, w.minutes.toFloat(), subjectColor(w.subject.color), goal = w.goalMinutes.toFloat().takeIf { it > 0 })
                            },
                            valueFormatter = { DateUtils.formatMinutes(it.toInt()) },
                        )
                    }
                }
            }

            if (state.progress.isNotEmpty()) {
                item { SectionTitle("진도 · 학급 진도 대비 복습률 (눌러서 단원 관리)") }
                items(state.progress, key = { it.subject.id }) { p ->
                    AppCard(onClick = { actions.onOpenSubject(p.subject.id) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ColorDot(subjectColor(p.subject.color), 10)
                                Spacer(Modifier.width(8.dp))
                                Text(p.subject.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text("학급 ${p.classCovered}/${p.total} · 복습 ${p.reviewed}/${p.total}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            LabeledProgress("복습률", if (p.classCovered == 0) 0f else p.reviewed.toFloat() / p.classCovered, subjectColor(p.subject.color))
                            if (p.reviewQueue.isNotEmpty()) Text("복습 필요: ${p.reviewQueue.joinToString { it.title }}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }

            item { SectionTitle("분석") }
            items(state.insights) { InsightCard(it, state.allSubjects, onAction = null) }

            if (state.recentGrades.isNotEmpty()) {
                item { SectionTitle("최근 성적") }
                items(state.recentGrades, key = { "g" + it.id }) { g ->
                    val subject = state.allSubjects.firstOrNull { it.id == g.subjectId }
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(g.title, style = MaterialTheme.typography.bodyLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    SubjectTag(subject)
                                    Text("${g.examType.label} · ${DateUtils.formatDate(DateUtils.fromEpochDay(g.date))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text("${g.percent.toInt()}점", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { SectionTitle("내가 낸 과제", action = { TextButton(onClick = { showAssign = true }) { Text("과제 내기") } }) }
            if (state.myTasks.isEmpty()) item { AppCard { EmptyState("미완료 과제가 없어요") } }
            else items(state.myTasks, key = { "t" + it.id }) { t ->
                val subject = state.allSubjects.firstOrNull { it.id == t.subjectId }
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(t.title, style = MaterialTheme.typography.bodyLarge)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(t.type.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                if (subject != null) SubjectTag(subject)
                                Text("마감 ${DateUtils.formatDate(DateUtils.fromEpochDay(t.dueDate))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        TextButton(onClick = { onEvent(MentorDashboardEvent.DeleteTask(t.id)) }) { Text("취소") }
                    }
                }
            }

            item { SectionTitle("피드백 · 메모", action = { TextButton(onClick = { showNote = true }) { Text("남기기") } }) }
            if (state.notes.isEmpty()) item { AppCard { EmptyState("학생에게 피드백을 남겨 보세요") } }
            else items(state.notes, key = { "n" + it.id }) { n ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(n.text, style = MaterialTheme.typography.bodyLarge)
                            Text("${n.authorName} (${Role.labelOf(n.authorRole)}) · ${DateUtils.formatDate(DateUtils.toLocalDate(n.createdAt))}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (n.authorRole == "MENTOR" && n.authorName == state.me?.name) TextButton(onClick = { onEvent(MentorDashboardEvent.DeleteNote(n.id)) }) { Text("삭제") }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showSubjects) {
        SubjectSelectDialog(state.allSubjects, state.me?.subjectIdList ?: emptyList(), onDismiss = { showSubjects = false }) { onEvent(MentorDashboardEvent.SetSubjects(it)) }
    }
    if (showAssign) {
        var title by remember { mutableStateOf("") }
        var subjectId by remember { mutableStateOf(state.subjects.firstOrNull()?.id) }
        var type by remember { mutableStateOf(TaskType.HOMEWORK) }
        var due by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
        AlertDialog(
            onDismissRequest = { showAssign = false },
            title = { Text("과제 내기") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("과제 내용") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    SubjectPicker(state.subjects, subjectId, onSelect = { subjectId = it })
                    OptionPicker(TaskType.entries, type, label = { it.label }, onSelect = { type = it })
                    DateField("마감", due, onChange = { due = it })
                }
            },
            confirmButton = { TextButton(enabled = title.isNotBlank(), onClick = { onEvent(MentorDashboardEvent.AssignTask(title.trim(), subjectId, type, due)); showAssign = false }) { Text("배정") } },
            dismissButton = { TextButton(onClick = { showAssign = false }) { Text("취소") } },
        )
    }
    if (showNote) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNote = false },
            title = { Text("피드백 남기기") },
            text = { OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("내용") }, modifier = Modifier.fillMaxWidth(), minLines = 2) },
            confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onEvent(MentorDashboardEvent.AddNote(text)); showNote = false }) { Text("저장") } },
            dismissButton = { TextButton(onClick = { showNote = false }) { Text("취소") } },
        )
    }
}
