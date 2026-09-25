package com.nextstep.app.ui.home

import com.nextstep.app.ui.components.speech.rememberSpeaker
import com.nextstep.app.ui.home.components.YearCard
import androidx.compose.ui.text.style.TextOverflow
import com.nextstep.app.ui.home.components.WeekCard
import com.nextstep.app.ui.home.components.LevelUpCard
import com.nextstep.app.ui.home.components.BigTaskRow
import com.nextstep.app.domain.growth.StudentHomeSection
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.common.ExternalLinks
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.CurriculumCard
import com.nextstep.app.ui.components.card.EmptyState
import com.nextstep.app.ui.components.card.JourneyNowCard
import com.nextstep.app.ui.components.card.MissionFocusCard
import com.nextstep.app.ui.components.card.LinkCard
import com.nextstep.app.ui.components.card.SectionTitle
import com.nextstep.app.ui.components.card.UpcomingExamCard
import com.nextstep.app.ui.components.row.EventRow
import com.nextstep.app.ui.components.row.TaskRow
import com.nextstep.app.ui.home.components.ActiveSubjectsCard
import com.nextstep.app.ui.home.components.PlanResultDialog
import com.nextstep.app.ui.home.components.PlannerDialog
import com.nextstep.app.ui.home.components.RecommendationCard
import com.nextstep.app.ui.home.components.RoadmapFocusRow
import com.nextstep.app.ui.home.components.TimerCard
import com.nextstep.app.ui.home.components.TopicSuggestionRow
import com.nextstep.app.domain.hub.ConcernSection

@Composable
fun StudentHomeScreen(actions: HomeActions, viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeContent(state = state, actions = actions, onEvent = viewModel::onEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(state: HomeUiState, actions: HomeActions, onEvent: (HomeEvent) -> Unit) {
    val context = LocalContext.current
    var showPlanner by remember { mutableStateOf(false) }
    val level = state.level
    val words = level.words
    val speak = if (level.kid.readsAloud) rememberSpeaker() else null
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("오늘", style = MaterialTheme.typography.titleLarge)
                        // 올해 한 줄: 해마다 바뀝니다(예: 초3 · 사회·과학·영어가 새로 시작되는 해).
                        state.year?.let { y -> Text("${y.label} · ${y.theme}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        Text(
                            if (!level.showsNumbers) DateUtils.formatFullDate(state.today)
                            else "${DateUtils.formatFullDate(state.today)} · 할 것 ${state.pendingTasks.size}개" + if (state.todayMinutes > 0) " · 오늘 ${DateUtils.formatMinutes(state.todayMinutes)}" else "",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { padding ->
        // 카드는 화면 단계(level)가 연 것만 그립니다. 학년으로 직접 분기하지 않습니다.
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(if (level.showsNumbers) 10.dp else 14.dp),
        ) {
            state.levelUp?.let { up -> item { LevelUpCard(up, state.newSections, onOk = { onEvent(HomeEvent.DismissLevelUp) }) } }
            // 카드 순서는 올해 프로필이 정합니다(StudentScreen.homeOrder): 해마다 먼저 보여 줄 카드가 다릅니다.
            state.homeOrder.forEach { section ->
                when (section) {
                    StudentHomeSection.TIMER -> item { TimerCard(state, words, big = !level.showsNumbers, goalMinutes = state.year?.dailyMinutes, onOpenTimer = actions.onOpenTimer) }
                    StudentHomeSection.YEAR -> state.year?.let { y -> item { YearCard(y, onAdd = { onEvent(HomeEvent.AddStudyKind(it)) }, onOpenYear = actions.onOpenYear) } }
                    StudentHomeSection.CURRICULUM -> state.curriculum?.let { c -> item { CurriculumCard(curriculum = c, periodLabel = state.periodLabel ?: "이번 학기", onOpen = actions.onOpenCurriculum) } }
                    StudentHomeSection.MISSION -> if (state.missionFocus.isNotEmpty()) item { MissionFocusCard(state.missionFocus, onOpen = actions.onOpenGoals) }
                    StudentHomeSection.JOURNEY -> if (state.hasBirthDate || state.journeyNow.isNotEmpty()) item { JourneyNowCard(items = state.journeyNow, today = state.today, hasBirthDate = state.hasBirthDate, onOpen = actions.onOpenJourney) }
                    StudentHomeSection.TASKS -> {
                        item { SectionTitle(words.tasksTitle, action = { TextButton(onClick = { actions.onOpenRecords(ConcernSection.CALENDAR) }) { Text("전체") } }) }
                        if (state.pendingTasks.isEmpty()) item { AppCard { EmptyState(words.allDone) } }
                        else items(state.pendingTasks.take(state.taskRows), key = { "task" + it.id }) { task ->
                            if (level.showsNumbers) TaskRow(task, state.subjects, onToggle = { onEvent(HomeEvent.ToggleTask(task)) })
                            else BigTaskRow(task, state.subjects, minHeightDp = level.touchTargetDp, onToggle = { onEvent(HomeEvent.ToggleTask(task)) }, onSpeak = speak)
                        }
                        if (state.pendingTasks.size > state.taskRows) item {
                            TextButton(onClick = { actions.onOpenRecords(ConcernSection.CALENDAR) }) { Text("${state.pendingTasks.size - state.taskRows}개 더 보기") }
                        }
                    }
                    StudentHomeSection.WEEK -> if (state.week.isNotEmpty()) item { WeekCard(state.week, state.streak, words.weekTitle, showsNumbers = level.showsNumbers) }
                    StudentHomeSection.EVENTS -> {
                        item { SectionTitle("오늘 일정") }
                        if (state.todayEvents.isEmpty()) item { AppCard { EmptyState("오늘은 등록된 일정이 없어요") } }
                        else items(state.todayEvents.take(UiDefaults.MAX_ROWS), key = { "ev" + it.event.id + it.startAt }) { occ -> EventRow(occ, state.subjects) }
                    }
                    StudentHomeSection.EXAM -> state.nextExam?.let { exam -> item { UpcomingExamCard(exam) } }
                    StudentHomeSection.RECOMMENDATION -> state.recommendations.firstOrNull()?.let { rec ->
                        item { SectionTitle("추천 영상 1개", action = { TextButton(onClick = actions.onOpenContent) { Text("저장소") } }) }
                        item { RecommendationCard(rec, onOpen = { ExternalLinks.open(context, rec.content.url) }, onWatched = { onEvent(HomeEvent.MarkContentWatched(rec.content.id)) }) }
                    }
                    StudentHomeSection.SUBJECTS -> if (state.activeSubjects.isNotEmpty()) {
                        item { SectionTitle("지금 배우는 과목", action = { TextButton(onClick = { actions.onOpenRecords(ConcernSection.PROGRESS) }) { Text("진도 전체") } }) }
                        item { ActiveSubjectsCard(state.activeSubjects.take(UiDefaults.MAX_ROWS), onOpenSubject = actions.onOpenSubject) }
                    }
                    StudentHomeSection.REVIEW -> if (state.reviewQueue.isNotEmpty()) {
                        item { SectionTitle(words.reviewTitle) }
                        items(state.reviewQueue.take(UiDefaults.MAX_ROWS), key = { "rv" + it.second.id }) { (subject, topic) ->
                            TopicSuggestionRow(subject, topic, actionLabel = words.reviewDone,
                                onAction = { onEvent(HomeEvent.MarkTopic(topic, TopicStatus.REVIEWED)) },
                                onAddTask = { onEvent(HomeEvent.AddQuickTask(subject, topic, TaskType.REVIEW)) },
                                onOpen = { actions.onOpenSubject(subject.id) })
                        }
                    }
                    StudentHomeSection.PREVIEW -> if (state.previewQueue.isNotEmpty()) {
                        item { SectionTitle(words.previewTitle) }
                        items(state.previewQueue.take(UiDefaults.MAX_ROWS), key = { "pv" + it.second.id }) { (subject, topic) ->
                            TopicSuggestionRow(subject, topic, actionLabel = words.previewDone,
                                onAction = { onEvent(HomeEvent.MarkTopic(topic, TopicStatus.PREVIEWED)) },
                                onAddTask = { onEvent(HomeEvent.AddQuickTask(subject, topic, TaskType.PREVIEW)) },
                                onOpen = { actions.onOpenSubject(subject.id) })
                        }
                    }
                    StudentHomeSection.ROADMAP -> if (state.roadmapFocus.isNotEmpty()) {
                        item { SectionTitle("멘토 로드맵", action = { TextButton(onClick = actions.onOpenRoadmap) { Text("전체 보기") } }) }
                        items(state.roadmapFocus, key = { "rm" + it.id }) { r ->
                            RoadmapFocusRow(r, state.subjects.firstOrNull { it.id == r.subjectId }, onOpen = actions.onOpenRoadmap, onStatus = { onEvent(HomeEvent.SetRoadmapStatus(r.id, it)) })
                        }
                    }
                    StudentHomeSection.PLANNER -> item { LinkCard("학습 계획 만들기", "밀린 복습, 멘토 로드맵, 다음 예습을 빈 시간에 자동으로 배치해요", onClick = { showPlanner = true }, actionLabel = "계획") }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    if (showPlanner) {
        PlannerDialog(defaults = state.planDefaults, onDismiss = { showPlanner = false }) { onEvent(HomeEvent.GeneratePlan(it)) }
    }
    state.lastPlan?.let { plan -> PlanResultDialog(plan, onDismiss = { onEvent(HomeEvent.DismissPlanResult) }) }
}
