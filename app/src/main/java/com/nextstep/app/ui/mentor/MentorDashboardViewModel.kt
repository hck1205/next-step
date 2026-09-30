package com.nextstep.app.ui.mentor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.BulkTaskRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.LessonRepository
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.ReportLogRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.family.byId
import com.nextstep.app.domain.feedback.FeedbackAudience
import com.nextstep.app.domain.feedback.FeedbackEngine
import com.nextstep.app.domain.feedback.FeedbackVoice
import com.nextstep.app.domain.growth.GrowthGuide
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.insight.InsightEngine
import com.nextstep.app.domain.lesson.Lessons
import com.nextstep.app.domain.mentor.AssignmentStats
import com.nextstep.app.domain.mentor.MentorScope
import com.nextstep.app.domain.report.LessonReports
import com.nextstep.app.domain.report.MentorStudy
import com.nextstep.app.domain.stats.RoadmapStats
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.stats.TrendStats
import com.nextstep.app.domain.task.TaskDrafts
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.common.actingRoleName
import com.nextstep.app.ui.common.asUiState
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** 멘토 대시보드. 담당 과목 범위(MentorScope)로 모든 지표를 좁힙니다. */
class MentorDashboardViewModel(
    private val streams: FamilyDataStreams,
    private val members: MemberRepository,
    private val tasks: TaskRepository,
    private val reportLogs: ReportLogRepository,
    private val bulk: BulkTaskRepository,
    private val lessons: LessonRepository,
) : ViewModel() {

    private val core = combine(streams.profile, streams.myMember, streams.members, streams.subjects, streams.syncStatus) { profile, me, members, subjects, sync ->
        val today = DateUtils.today()
        val stage = GrowthStage.of(members, today)
        MentorDashboardUiState(
            me = me,
            studentName = profile.studentName,
            students = profile.children,
            activeFamilyId = profile.familyId,
            syncStatus = sync,
            allSubjects = subjects,
            subjects = MentorScope.of(me, subjects).subjects,
            otherMentors = members.filter { it.isMentor && it.id != me?.id },
            stage = stage,
            mentorTip = stage?.let { GrowthGuide.pickForDay(GrowthGuide.forStage(it).mentorTips, today) },
        )
    }

    private val data = combine(streams.topics, streams.grades, streams.sessions, streams.tasks, streams.events) { t, g, s, ta, e -> Data(t, g, s, ta, e) }

    val state: StateFlow<MentorDashboardUiState> = combine(core, data, streams.roadmap, streams.reportLogs, streams.lessons) { s, d, roadmap, logs, lessonRecords ->
        val scope = MentorScope(s.subjects)
        val grades = scope.own(d.grades) { it.subjectId }
        val sessions = scope.own(d.sessions) { it.subjectId }
        val topics = scope.own(d.topics) { it.subjectId }
        val scopedTasks = scope.ownOrGeneral(d.tasks) { it.subjectId }
        val today = DateUtils.today()
        val findings = FeedbackEngine.findings(s.subjects, topics, grades, sessions, scopedTasks, today)
        val weekly = StudyStats.weeklyMinutesBySubject(sessions, s.subjects).filter { it.subject != null }
        val book = s.me?.let { Lessons.bookOf(it, lessonRecords, YearMonth.from(today), today) }
        val progress = StudyStats.subjectProgress(topics, s.subjects)
        val study = MentorStudy(s.studentName, s.me?.name.orEmpty(), s.subjects, sessions, progress, scopedTasks, findings)
        s.copy(
            weeklyBySubject = weekly,
            progress = progress,
            recentGrades = grades.take(UiDefaults.MAX_RECENT_RECORDS),
            myTasks = scopedTasks.filter { !it.done && AssignmentStats.isAssignment(it) },
            insights = InsightEngine.analyze(s.subjects, topics, grades, sessions, scopedTasks, d.events).take(UiDefaults.MAX_INSIGHTS),
            roadmap = RoadmapStats.summarize(roadmap, DateUtils.today()),
            trends = TrendStats.family(sessions, scopedTasks, grades, s.subjects, DateUtils.today()),
            feedback = FeedbackVoice.lines(findings, FeedbackAudience.MENTOR),
            report = LessonReports.week(study, today),
            monthReport = LessonReports.month(study, today),
            reportLogs = logs.filter { it.sentById == s.me?.id }.take(UiDefaults.MAX_ROWS), // 멘토 화면은 원본 스트림이라 여기서 내 것만
            lessons = book,
            lessonToday = book?.on(today),
        )
    }.asUiState(viewModelScope, MentorDashboardUiState())

    fun setSubjects(ids: List<String>) {
        viewModelScope.launch {
            val me = state.value.me ?: return@launch
            members.setSubjects(me.id, ids)
        }
    }

    /** 과제 내기. [alsoTo] 가 있으면 맡은 다른 학생들(가족 id)에게도 같은 과제를(과목은 이름으로 맞춤). */
    fun assignTask(title: String, subjectId: String?, type: TaskType, due: LocalDate, alsoTo: List<String> = emptyList()) {
        viewModelScope.launch {
            val draft = TaskDrafts.written(title, subjectId, type, due, streams.actingRoleName())
            tasks.save(draft)
            if (alsoTo.isNotEmpty()) bulk.assign(draft, alsoTo, state.value.allSubjects.byId(subjectId)?.name)
        }
    }

    fun deleteTask(id: String) { viewModelScope.launch { tasks.delete(id) } }

    private data class Data(
        val topics: List<TopicEntity>,
        val grades: List<GradeEntity>,
        val sessions: List<StudySessionEntity>,
        val tasks: List<TaskEntity>,
        val events: List<EventEntity>,
    )

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: MentorDashboardEvent) {
        when (event) {
            is MentorDashboardEvent.SetSubjects -> setSubjects(event.ids)
            is MentorDashboardEvent.AssignTask -> assignTask(event.title, event.subjectId, event.type, event.due, event.alsoTo)
            is MentorDashboardEvent.ReportSent -> viewModelScope.launch { reportLogs.record(event.kind, event.title, state.value.me?.name.orEmpty()) }
            is MentorDashboardEvent.DeleteTask -> deleteTask(event.id)
            is MentorDashboardEvent.MarkLesson -> viewModelScope.launch { lessons.mark(event.date, event.status) }
            is MentorDashboardEvent.SaveLessonPlan -> viewModelScope.launch { state.value.me?.let { members.setLessonPlan(it.id, event.plan) } }
        }
    }
}
