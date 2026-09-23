package com.nextstep.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.JourneyNowCard
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.EventRow
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.SubjectTag
import com.nextstep.app.ui.components.TaskRow
import com.nextstep.app.ui.home.components.PlannerDialog
import com.nextstep.app.ui.home.components.TimerCard
import com.nextstep.app.ui.home.components.TopicSuggestionRow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun StudentHomeScreen(actions: HomeActions, viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

private const val MAX_ROWS = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(state: HomeUiState, actions: HomeActions, onEvent: (HomeEvent) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showPlanner by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("오늘", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${DateUtils.formatFullDate(state.today)} · 할 것 ${state.pendingTasks.size}개" + if (state.todayMinutes > 0) " · 오늘 ${DateUtils.formatMinutes(state.todayMinutes)}" else "",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
            item { TimerCard(state, actions.onOpenTimer) }
            if (state.hasBirthDate || state.journeyNow.isNotEmpty()) item { JourneyNowCard(items = state.journeyNow, today = state.today, hasBirthDate = state.hasBirthDate, onOpen = actions.onOpenJourney) }

            item { SectionTitle("오늘 할 것", action = { TextButton(onClick = { actions.onOpenRecords(com.nextstep.app.ui.records.RecordSegment.CALENDAR) }) { Text("전체") } }) }
            if (state.pendingTasks.isEmpty()) item { AppCard { EmptyState("할 일을 모두 끝냈어요") } }
            else items(state.pendingTasks.take(MAX_ROWS), key = { "task" + it.id }) { task -> TaskRow(task, state.subjects, onToggle = { onEvent(HomeEvent.ToggleTask(task)) }) }
            if (state.pendingTasks.size > MAX_ROWS) item {
                TextButton(onClick = { actions.onOpenRecords(com.nextstep.app.ui.records.RecordSegment.CALENDAR) }) { Text("${state.pendingTasks.size - MAX_ROWS}개 더 보기") }
            }

            item { SectionTitle("오늘 일정") }
            if (state.todayEvents.isEmpty()) item { AppCard { EmptyState("오늘은 등록된 일정이 없어요") } }
            else items(state.todayEvents.take(MAX_ROWS), key = { "ev" + it.event.id + it.startAt }) { occ -> EventRow(occ, state.subjects) }
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

            state.latestNote?.let { note ->
                item {
                    AppCard {
                        Column {
                            Text("${note.authorName}의 한마디", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("“${note.text}”", style = MaterialTheme.typography.bodyLarge)
                            Text(DateUtils.formatDate(DateUtils.toLocalDate(note.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            state.recommendations.firstOrNull()?.let { rec ->
                item { SectionTitle("추천 영상 1개", action = { TextButton(onClick = actions.onOpenContent) { Text("저장소") } }) }
                item {
                    AppCard(onClick = { runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(rec.content.url))) } }) {
                        Column {
                            Text(rec.reason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(rec.content.title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(listOf(rec.content.channel, rec.content.subjectKey, rec.content.contentType.label).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                TextButton(onClick = { onEvent(HomeEvent.MarkContentWatched(rec.content.id)) }) { Text("봤어요") }
                            }
                        }
                    }
                }
            }

            if (state.activeSubjects.isNotEmpty()) {
                item { SectionTitle("지금 배우는 과목", action = { TextButton(onClick = { actions.onOpenRecords(com.nextstep.app.ui.records.RecordSegment.PROGRESS) }) { Text("진도 전체") } }) }
                item {
                    AppCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            state.activeSubjects.take(MAX_ROWS).forEach { p ->
                                val current = p.reviewQueue.firstOrNull() ?: p.previewQueue.firstOrNull()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SubjectTag(p.subject)
                                    Spacer(Modifier.width(8.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(current?.title ?: "다음 단원 없음", style = MaterialTheme.typography.bodyMedium)
                                        Text("학급 ${p.classCovered}/${p.total} 단원 · 복습 ${p.reviewed}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    TextButton(onClick = { actions.onOpenSubject(p.subject.id) }) { Text("열기") }
                                }
                            }
                        }
                    }
                }
            }

            if (state.reviewQueue.isNotEmpty()) {
                item { SectionTitle("복습할 단원") }
                items(state.reviewQueue.take(MAX_ROWS), key = { "rv" + it.second.id }) { (subject, topic) ->
                    TopicSuggestionRow(subject, topic, actionLabel = "복습 완료",
                        onAction = { onEvent(HomeEvent.MarkTopic(topic, TopicStatus.REVIEWED)) },
                        onAddTask = { onEvent(HomeEvent.AddQuickTask(subject, topic, TaskType.REVIEW)) },
                        onOpen = { actions.onOpenSubject(subject.id) })
                }
            }
            if (state.previewQueue.isNotEmpty()) {
                item { SectionTitle("예습할 단원") }
                items(state.previewQueue.take(MAX_ROWS), key = { "pv" + it.second.id }) { (subject, topic) ->
                    TopicSuggestionRow(subject, topic, actionLabel = "예습 완료",
                        onAction = { onEvent(HomeEvent.MarkTopic(topic, TopicStatus.PREVIEWED)) },
                        onAddTask = { onEvent(HomeEvent.AddQuickTask(subject, topic, TaskType.PREVIEW)) },
                        onOpen = { actions.onOpenSubject(subject.id) })
                }
            }

            if (state.roadmapFocus.isNotEmpty()) {
                item { SectionTitle("멘토 로드맵", action = { TextButton(onClick = actions.onOpenRoadmap) { Text("전체 보기") } }) }
                items(state.roadmapFocus, key = { "rm" + it.id }) { r ->
                    val subject = state.subjects.firstOrNull { it.id == r.subjectId }
                    AppCard(onClick = actions.onOpenRoadmap) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(r.title, style = MaterialTheme.typography.bodyLarge)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    if (subject != null) SubjectTag(subject)
                                    Text(r.status.label + (r.targetDate?.let { " · ${DateUtils.dDay(DateUtils.fromEpochDay(it))}" } ?: ""), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    if (r.createdByName.isNotBlank()) Text("${r.createdByName} 제안", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            if (r.status == RoadmapStatus.PLANNED) TextButton(onClick = { onEvent(HomeEvent.SetRoadmapStatus(r.id, RoadmapStatus.IN_PROGRESS)) }) { Text("시작") }
                            else TextButton(onClick = { onEvent(HomeEvent.SetRoadmapStatus(r.id, RoadmapStatus.DONE)) }) { Text("완료") }
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
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showPlanner) {
        PlannerDialog(defaults = state.planDefaults, onDismiss = { showPlanner = false }) { onEvent(HomeEvent.GeneratePlan(it)) }
    }
    state.lastPlan?.let { plan ->
        AlertDialog(
            onDismissRequest = { onEvent(HomeEvent.DismissPlanResult) },
            title = { Text(if (plan.isEmpty) "배치할 항목이 없어요" else "학습 계획 완성") },
            text = {
                Text(
                    if (plan.isEmpty) "복습·예습할 단원이나 로드맵 항목이 없거나, 빈 시간이 없어요. 커리큘럼에서 단원과 학급 진도를 등록해 보세요."
                    else "${plan.events.size}개의 자습 일정과 할 일을 캘린더에 넣었어요. 첫 일정: ${DateUtils.formatDate(DateUtils.toLocalDate(plan.events.first().startAt))} ${DateUtils.formatTime(plan.events.first().startAt)} ${plan.events.first().title}",
                )
            },
            confirmButton = { TextButton(onClick = { onEvent(HomeEvent.DismissPlanResult) }) { Text("확인") } },
        )
    }
}
