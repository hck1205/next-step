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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.nextstep.app.ui.components.EmptyState
import com.nextstep.app.ui.components.EventRow
import com.nextstep.app.ui.components.LabeledProgress
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.StatTile
import com.nextstep.app.ui.components.SubjectTag
import com.nextstep.app.ui.components.TaskRow
import com.nextstep.app.ui.components.subjectColor
import com.nextstep.app.ui.home.components.PlannerDialog
import com.nextstep.app.ui.home.components.TimerCard
import com.nextstep.app.ui.home.components.TopicSuggestionRow

@Composable
fun StudentHomeScreen(actions: HomeActions, viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

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
                        Text("안녕, ${state.displayName.ifBlank { "학생" }}!", style = MaterialTheme.typography.titleLarge)
                        Text(DateUtils.formatFullDate(DateUtils.today()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            item { TimerCard(state, actions.onOpenTimer) }

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
                                    TextButton(onClick = { actions.onOpenSubject(p.subject.id) }) { Text("열기") }
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
                item { SectionTitle("멘토 로드맵", action = { TextButton(onClick = actions.onOpenRoadmap) { Text("전체 보기") } }) }
                items(state.roadmapFocus, key = { "rm" + it.id }) { r ->
                    val subject = state.subjects.firstOrNull { it.id == r.subjectId }
                    AppCard(onClick = actions.onOpenRoadmap) {
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
                                if (r.status == RoadmapStatus.PLANNED) TextButton(onClick = { onEvent(HomeEvent.SetRoadmapStatus(r.id, RoadmapStatus.IN_PROGRESS)) }) { Text("시작") }
                                else TextButton(onClick = { onEvent(HomeEvent.SetRoadmapStatus(r.id, RoadmapStatus.DONE)) }) { Text("완료") }
                            }
                        }
                    }
                }
            }

            item { SectionTitle("추천 영상", action = { TextButton(onClick = actions.onOpenContent) { Text("저장소") } }) }
            if (state.recommendations.isEmpty()) item {
                AppCard(onClick = actions.onOpenContent) { Text("콘텐츠 저장소에 유튜브 링크를 등록하면 지금 배우는 단원에 맞는 영상을 골라 줘요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            items(state.recommendations, key = { "rec" + it.content.id }) { rec ->
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

            item { SectionTitle("오늘 일정") }
            if (state.todayEvents.isEmpty()) item { AppCard { EmptyState("오늘은 등록된 일정이 없어요") } }
            else items(state.todayEvents, key = { "ev" + it.event.id + it.startAt }) { occ -> EventRow(occ, state.subjects) }

            item { SectionTitle("오늘 할 일") }
            if (state.pendingTasks.isEmpty()) item { AppCard { EmptyState("할 일을 모두 끝냈어요 🎉") } }
            else items(state.pendingTasks, key = { "task" + it.id }) { task -> TaskRow(task, state.subjects, onToggle = { onEvent(HomeEvent.ToggleTask(task)) }) }

            if (state.reviewQueue.isNotEmpty()) {
                item { SectionTitle("복습할 단원") }
                items(state.reviewQueue, key = { "rv" + it.second.id }) { (subject, topic) ->
                    TopicSuggestionRow(subject, topic, actionLabel = "복습 완료",
                        onAction = { onEvent(HomeEvent.MarkTopic(topic, TopicStatus.REVIEWED)) },
                        onAddTask = { onEvent(HomeEvent.AddQuickTask(subject, topic, TaskType.REVIEW)) },
                        onOpen = { actions.onOpenSubject(subject.id) })
                }
            }
            if (state.previewQueue.isNotEmpty()) {
                item { SectionTitle("예습할 단원") }
                items(state.previewQueue, key = { "pv" + it.second.id }) { (subject, topic) ->
                    TopicSuggestionRow(subject, topic, actionLabel = "예습 완료",
                        onAction = { onEvent(HomeEvent.MarkTopic(topic, TopicStatus.PREVIEWED)) },
                        onAddTask = { onEvent(HomeEvent.AddQuickTask(subject, topic, TaskType.PREVIEW)) },
                        onOpen = { actions.onOpenSubject(subject.id) })
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
        PlannerDialog(onDismiss = { showPlanner = false }) { onEvent(HomeEvent.GeneratePlan(it)) }
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
