package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.domain.content.ContentRecommendation
import com.nextstep.app.domain.growth.StudentHomeSection
import com.nextstep.app.domain.hub.ConcernSection
import com.nextstep.app.domain.selfdirection.WeekStatus
import com.nextstep.app.ui.common.ExternalLinks
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.CurriculumCard
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.GameCard
import com.nextstep.app.ui.components.card.JourneyNowCard
import com.nextstep.app.ui.components.card.LinkCard
import com.nextstep.app.ui.components.card.MissionFocusCard
import com.nextstep.app.ui.components.card.RoutineCard
import com.nextstep.app.ui.components.card.UpcomingExamCard
import com.nextstep.app.ui.components.card.WeekPlanCard
import com.nextstep.app.ui.components.chart.ScoreMultiples
import com.nextstep.app.ui.components.row.EventRow
import com.nextstep.app.ui.components.row.TaskRow
import com.nextstep.app.ui.home.HomeActions
import com.nextstep.app.ui.home.HomeEvent
import com.nextstep.app.ui.home.HomeUiState

/**
 * 학생 오늘 화면의 카드 한 장의 내용. 같은 내용을 슬라이드(줄인 모양 [compact]) · 관심사 칩으로 고른 목록 · 자세히 시트(전부)에서 씁니다.
 * 제목은 [homeSectionTitle] 이 정하고(카드가 스스로 머리를 가지면 빈 문자열), 틀(TodayCardFrame)이 그립니다.
 */
@Composable
internal fun HomeSectionBody(
    section: StudentHomeSection, state: HomeUiState, actions: HomeActions, onEvent: (HomeEvent) -> Unit, compact: Boolean,
    onSpeak: ((String) -> Unit)?, onOpenPlanner: () -> Unit,
) {
    val level = state.level
    val words = level.words
    val rows = if (compact) UiDefaults.MAX_ROWS else FULL_ROWS
    when (section) {
        StudentHomeSection.TIMER -> TimerCard(state, words, big = !level.showsNumbers, goalMinutes = state.year?.dailyMinutes, onOpenTimer = actions.onOpenTimer)
        StudentHomeSection.YEAR -> state.year?.let { y ->
            YearCard(y, aheadCount = state.yearAheadCount, aheadHeading = state.yearAheadHeading, onAdd = { onEvent(HomeEvent.AddStudyKind(it)) }, onOpenYear = actions.onOpenYear)
        }
        StudentHomeSection.CURRICULUM -> state.curriculum?.let { c -> CurriculumCard(curriculum = c, periodLabel = state.periodLabel ?: "이번 학기", onOpen = actions.onOpenCurriculum) }
        StudentHomeSection.MISSION -> MissionFocusCard(state.missionFocus, onOpen = actions.onOpenGoals)
        StudentHomeSection.JOURNEY -> JourneyNowCard(items = state.journeyNow, today = state.today, hasBirthDate = state.hasBirthDate, onOpen = actions.onOpenJourney)
        StudentHomeSection.TASKS -> TodayTasks(state, shown = if (compact) state.taskRows else FULL_ROWS, onEvent, onSpeak, onOpenAll = if (compact) null else ({ actions.onOpenRecords(ConcernSection.CALENDAR) }))
        StudentHomeSection.MY_WEEK -> state.myWeek?.let { week -> MyWeek(week, state, onEvent, onOpen = { actions.onOpenRecords(ConcernSection.SELF) }) }
        StudentHomeSection.ROUTINE -> RoutineCard(
            state.routines, onToggle = { p, item -> onEvent(HomeEvent.ToggleRoutine(p, item)) }, onOpen = actions.onOpenProject,
            big = !level.showsNumbers, compact = compact,
        )
        StudentHomeSection.GAME -> state.game?.let { g ->
            GameCard(g, state.nextReward, showsNumbers = level.showsNumbers, onOpen = { actions.onOpenRecords(ConcernSection.REWARDS) })
        }
        StudentHomeSection.WEEK -> WeekCard(state.week, state.streak, words.weekTitle, showsNumbers = level.showsNumbers)
        StudentHomeSection.STUDY_FLOW -> StudyFlowCard(state.studyHeat, state.week, state.year?.dailyMinutes ?: 0, state.today, compact)
        StudentHomeSection.MY_SCORES -> AppCard { ScoreMultiples(state.scoreSeries, lineHeight = if (compact) COMPACT_LINE_DP.dp else FULL_LINE_DP.dp) }
        StudentHomeSection.EVENTS -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (state.todayEvents.isEmpty()) EmptyCard("오늘은 등록된 일정이 없어요")
            else state.todayEvents.take(rows).forEach { occ -> EventRow(occ, state.subjects) }
        }
        StudentHomeSection.EXAM -> state.nextExam?.let { UpcomingExamCard(it) }
        StudentHomeSection.RECOMMENDATION -> state.recommendations.firstOrNull()?.let { rec -> Recommendation(rec, onEvent, onOpenLibrary = if (compact) null else actions.onOpenContent) }
        StudentHomeSection.SUBJECTS -> Column {
            ActiveSubjectsCard(state.activeSubjects.take(rows), onOpenSubject = actions.onOpenSubject)
            if (!compact) TextButton(onClick = { actions.onOpenRecords(ConcernSection.PROGRESS) }) { Text("진도 전체") }
        }
        StudentHomeSection.REVIEW -> TopicQueue(state.reviewQueue.take(rows), words.reviewDone, TopicStatus.REVIEWED, TaskType.REVIEW, onEvent, actions.onOpenSubject)
        StudentHomeSection.PREVIEW -> TopicQueue(state.previewQueue.take(rows), words.previewDone, TopicStatus.PREVIEWED, TaskType.PREVIEW, onEvent, actions.onOpenSubject)
        StudentHomeSection.ROADMAP -> RoadmapFocus(state, rows, onEvent, actions.onOpenRoadmap, showAll = !compact)
        StudentHomeSection.PLANNER -> LinkCard("학습 계획 만들기", "밀린 복습, 멘토 로드맵, 다음 예습을 빈 시간에 자동으로 배치해요", onClick = onOpenPlanner, actionLabel = "계획")
    }
}

/** 오늘 할 일: 숫자를 보는 나이는 한 줄, 어린 나이는 큰 줄(읽어 주기). 자세히 보기에서는 아래에 남은 수와 전체 보기([onOpenAll]). */
@Composable
private fun TodayTasks(state: HomeUiState, shown: Int, onEvent: (HomeEvent) -> Unit, onSpeak: ((String) -> Unit)?, onOpenAll: (() -> Unit)?) {
    val level = state.level
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (state.pendingTasks.isEmpty()) EmptyCard(level.words.allDone)
        else state.pendingTasks.take(shown).forEach { task ->
            if (level.showsNumbers) TaskRow(task, state.subjects, onToggle = { onEvent(HomeEvent.ToggleTask(task)) })
            else BigTaskRow(task, state.subjects, minHeightDp = level.touchTargetDp, onToggle = { onEvent(HomeEvent.ToggleTask(task)) }, onSpeak = onSpeak)
        }
        if (onOpenAll != null) TextButton(onClick = onOpenAll) {
            Text(if (state.pendingTasks.size > shown) "${state.pendingTasks.size - shown}개 더 · 전체 보기" else "전체 보기")
        }
    }
}

/** 나의 이번 주. 학생 화면이라 확인(승인) 버튼은 쓰지 않습니다. */
@Composable
private fun MyWeek(week: WeekStatus, state: HomeUiState, onEvent: (HomeEvent) -> Unit, onOpen: () -> Unit) {
    WeekPlanCard(
        week = week, access = state.myWeekAccess, big = !state.level.showsNumbers,
        onSavePlan = { goals, minutes -> onEvent(HomeEvent.SaveWeekPlan(goals, minutes)) },
        onToggle = { id, i -> onEvent(HomeEvent.ToggleWeekGoal(id, i)) }, onApprove = {},
        onReflect = { w, mood, good, hard, change -> onEvent(HomeEvent.ReflectWeek(w, mood, good, hard, change)) },
        onOpen = onOpen,
    )
}

@Composable
private fun Recommendation(rec: ContentRecommendation, onEvent: (HomeEvent) -> Unit, onOpenLibrary: (() -> Unit)?) {
    val context = LocalContext.current
    Column {
        RecommendationCard(rec, onOpen = { ExternalLinks.open(context, rec.content.url) }, onWatched = { onEvent(HomeEvent.MarkContentWatched(rec.content.id)) })
        if (onOpenLibrary != null) TextButton(onClick = onOpenLibrary) { Text("영상 저장소") }
    }
}

/** 다시 보기(복습)·미리 보기(예습) 줄. 끝냈다고 표시하거나 할 일로 넣습니다. */
@Composable
private fun TopicQueue(
    queue: List<Pair<SubjectEntity, TopicEntity>>, actionLabel: String, doneStatus: TopicStatus, taskType: TaskType,
    onEvent: (HomeEvent) -> Unit, onOpenSubject: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        queue.forEach { (subject, topic) ->
            TopicSuggestionRow(
                subject, topic, actionLabel = actionLabel,
                onAction = { onEvent(HomeEvent.MarkTopic(topic, doneStatus)) },
                onAddTask = { onEvent(HomeEvent.AddQuickTask(subject, topic, taskType)) },
                onOpen = { onOpenSubject(subject.id) },
            )
        }
    }
}

@Composable
private fun RoadmapFocus(state: HomeUiState, rows: Int, onEvent: (HomeEvent) -> Unit, onOpenRoadmap: () -> Unit, showAll: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        state.roadmapFocus.take(rows).forEach { r ->
            RoadmapFocusRow(r, state.subjects.firstOrNull { it.id == r.subjectId }, onOpen = onOpenRoadmap, onStatus = { onEvent(HomeEvent.SetRoadmapStatus(r.id, it)) })
        }
        if (showAll) TextButton(onClick = onOpenRoadmap) { Text("전체 보기") }
    }
}

/** 카드 틀의 제목. 카드가 스스로 머리를 가진 것(올해의 공부·나의 이번 주·레벨·계획 만들기 …)은 빈 문자열이라 펼치기 버튼만 붙습니다. */
internal fun homeSectionTitle(section: StudentHomeSection, state: HomeUiState): String {
    val words = state.level.words
    return when (section) {
        StudentHomeSection.TASKS -> words.tasksTitle
        StudentHomeSection.REVIEW -> words.reviewTitle
        StudentHomeSection.PREVIEW -> words.previewTitle
        StudentHomeSection.ROUTINE, StudentHomeSection.EVENTS, StudentHomeSection.MISSION, StudentHomeSection.RECOMMENDATION,
        StudentHomeSection.SUBJECTS, StudentHomeSection.ROADMAP, StudentHomeSection.STUDY_FLOW, StudentHomeSection.MY_SCORES -> section.label
        StudentHomeSection.TIMER, StudentHomeSection.YEAR, StudentHomeSection.CURRICULUM, StudentHomeSection.JOURNEY, StudentHomeSection.MY_WEEK,
        StudentHomeSection.GAME, StudentHomeSection.WEEK, StudentHomeSection.EXAM, StudentHomeSection.PLANNER -> ""
    }
}

private const val FULL_ROWS = 10
private const val COMPACT_LINE_DP = 26
private const val FULL_LINE_DP = 40
