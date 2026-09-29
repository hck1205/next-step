package com.nextstep.app.ui.mentor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.lesson.LessonPlan
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.domain.today.MentorTodayCard
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.card.FeedbackCard
import com.nextstep.app.ui.components.card.InsightCard
import com.nextstep.app.ui.components.card.LinkCard
import com.nextstep.app.ui.components.card.ScoreTrendCard
import com.nextstep.app.ui.components.card.StageCard
import com.nextstep.app.ui.components.card.StudyWeeksCard
import com.nextstep.app.ui.components.card.SubjectTimeCard
import com.nextstep.app.ui.components.card.SubmissionsCard
import com.nextstep.app.ui.mentor.MentorDashboardActions
import com.nextstep.app.ui.mentor.MentorDashboardEvent
import com.nextstep.app.ui.mentor.MentorDashboardUiState

/** 멘토 오늘 화면의 카드 한 장의 내용. "먼저 볼 것"에서는 줄인 모양([compact]), 관심사를 펼친 목록·자세히 시트에서는 전부. */
@Composable
internal fun MentorCardBody(
    card: MentorTodayCard, state: MentorDashboardUiState, actions: MentorDashboardActions, onEvent: (MentorDashboardEvent) -> Unit, compact: Boolean,
    onChangeSubjects: () -> Unit, onAssign: () -> Unit,
) {
    val rows = if (compact) UiDefaults.MAX_ROWS else FULL_ROWS
    when (card) {
        MentorTodayCard.FEEDBACK -> FeedbackCard(state.feedback, compact = compact)
        MentorTodayCard.REPORT -> state.report?.let { week ->
            LessonReportCard(week, state.monthReport, state.me?.signature.orEmpty(), state.reportLogs, state.entitlements, compact) { kind, title ->
                onEvent(MentorDashboardEvent.ReportSent(kind, title))
            }
        }
        MentorTodayCard.LESSONS -> LessonTodayCard(
            state.lessons, state.lessonToday, state.me?.let(LessonPlan::of) ?: LessonPlan(emptySet(), 0, 0),
            onMark = { d, s -> onEvent(MentorDashboardEvent.MarkLesson(d, s)) }, onSavePlan = { onEvent(MentorDashboardEvent.SaveLessonPlan(it)) },
        )
        MentorTodayCard.STAGE -> StageCard(stage = state.stage, gradeLabel = null, headline = state.stage?.let { "이 시기의 큐레이팅 기준" }, body = state.mentorTip, experience = null, onSetGrade = actions.onOpenJourney)
        MentorTodayCard.INSIGHTS -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.insights.take(if (compact) 1 else rows).forEach { InsightCard(it, state.allSubjects, onAction = null) }
        }
        MentorTodayCard.SUBJECTS -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MentorSubjectsCard(state.subjects, state.needsSubjectSetup, onChange = onChangeSubjects)
            if (state.otherMentors.isNotEmpty()) {
                Text(
                    "함께 연결된 멘토: " + state.otherMentors.joinToString { m -> m.name + (if (m.title.isNotBlank()) " (${m.title})" else "") },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        MentorTodayCard.TASKS -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (state.myTasks.isEmpty()) EmptyCard("미완료 과제가 없어요")
            state.myTasks.take(rows).forEach { t ->
                MentorTaskRow(t, state.allSubjects.firstOrNull { it.id == t.subjectId }, onCancel = { onEvent(MentorDashboardEvent.DeleteTask(t.id)) })
            }
            TextButton(onClick = onAssign) { Text(if (state.myTasks.size > rows) "${state.myTasks.size - rows}개 더 · 과제 내기" else "과제 내기") }
        }
        MentorTodayCard.SUBMISSIONS -> SubmissionsCard(state.trends.submissions)
        MentorTodayCard.STUDY_WEEKS -> StudyWeeksCard(state.trends.rolling, state.subjects.sumOf { it.weeklyGoalMinutes }, DateUtils.today(), compact)
        MentorTodayCard.WEEK_CHART -> SubjectTimeCard(state.weeklyBySubject)
        MentorTodayCard.SCORES -> ScoreTrendCard(state.trends.scores, compact)
        MentorTodayCard.PROGRESS -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.progress.take(if (compact) 2 else rows).forEach { p -> MentorProgressCard(p, onOpen = { actions.onOpenSubject(p.subject.id) }) }
        }
        MentorTodayCard.ROADMAP -> {
            val r = state.roadmap
            LinkCard(
                title = "학습 로드맵 큐레이팅",
                description = if (r.isEmpty) "무엇을 어떤 순서로, 어떤 자료로, 언제까지 공부할지 제안해 보세요"
                else "진행 중 ${r.inProgress} · 완료 ${r.done}/${r.total}" + (if (r.overdue > 0) " · 기한 지남 ${r.overdue}" else ""),
                onClick = actions.onOpenRoadmap,
            )
        }
        MentorTodayCard.CONTENT -> LinkCard("콘텐츠 저장소", "좋은 유튜브 강의를 링크로 등록하면 자동 분류되고 학생 진도에 맞춰 추천돼요", onClick = actions.onOpenContent)
        MentorTodayCard.GRADES -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            state.recentGrades.take(rows).forEach { g -> MentorGradeRow(g, state.allSubjects.firstOrNull { it.id == g.subjectId }) }
        }
    }
}

/** 카드 틀의 제목. 스스로 머리를 가진 카드(기준·로드맵·콘텐츠)는 빈 문자열. */
internal fun mentorCardTitle(card: MentorTodayCard): String = when (card) {
    MentorTodayCard.STAGE, MentorTodayCard.ROADMAP, MentorTodayCard.CONTENT -> ""
    MentorTodayCard.PROGRESS -> "진도 · 학급 진도 대비 복습률"
    MentorTodayCard.INSIGHTS, MentorTodayCard.FEEDBACK, MentorTodayCard.REPORT, MentorTodayCard.LESSONS, MentorTodayCard.SUBJECTS, MentorTodayCard.TASKS, MentorTodayCard.GRADES, MentorTodayCard.WEEK_CHART,
    MentorTodayCard.SUBMISSIONS, MentorTodayCard.STUDY_WEEKS, MentorTodayCard.SCORES,
    -> card.title
}

private const val FULL_ROWS = 10
