package com.nextstep.app.ui.parent

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.domain.mission.MissionPlanner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.NoteRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.insight.InsightEngine
import com.nextstep.app.domain.insight.TalentEngine
import com.nextstep.app.domain.stats.StudyStats
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.domain.growth.GrowthGuide
import com.nextstep.app.domain.journey.JourneyPlanner
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.stats.BalanceStats
import com.nextstep.app.domain.stats.RoadmapStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.common.asUiState

/** 학부모 첫 화면. 상태 문장·지금 챙길 것·오늘의 아이·격려·재능/분석 한 줄. */
class ParentDashboardViewModel(
    private val streams: FamilyDataStreams,
    private val tasks: TaskRepository,
    private val notes: NoteRepository,
) : ViewModel() {

    private val core = combine(streams.profile, streams.subjects, streams.sessions, streams.tasks, streams.events) { profile, subjects, sessions, tasks, events ->
        ParentDashboardUiState(
            parentName = profile.displayName,
            studentName = profile.studentName,
            subjects = subjects,
            todayMinutes = StudyStats.todayMinutes(sessions),
            weekMinutes = StudyStats.weekMinutes(sessions),
            weekGoalMinutes = subjects.sumOf { it.weeklyGoalMinutes },
            daily = StudyStats.dailyMinutes(sessions, 7),
            weeklyBySubject = StudyStats.weeklyMinutesBySubject(sessions, subjects),
            pendingTasks = StudyStats.pendingTasks(tasks),
            overdueCount = StudyStats.overdueTasks(tasks).size,
            upcomingExams = StudyStats.upcomingExams(events, tasks).take(UiDefaults.MAX_ROWS),
        )
    }

    private val data = combine(streams.grades, streams.topics, streams.notes, streams.sessions, streams.tasks) { grades, topics, notes, sessions, tasks ->
        Extra(grades, topics, notes, sessions, tasks)
    }

    private val extra = combine(streams.events, streams.syncStatus, streams.roadmap, streams.members, streams.journeyItems) { e, s, r, m, j -> Side(e, s, r, m, j) }
        .let { side -> combine(side, streams.activities, streams.goals, streams.goalSteps) { x, a, g, st -> x.copy(activities = a, goals = g, goalSteps = st) } }

    val state: StateFlow<ParentDashboardUiState> = combine(core, data, extra) { s, d, x ->
        val events = x.events
        val today = DateUtils.today()
        val ctx = StudentContext.of(x.members, today)
        val stage = ctx.stage
        val period = ctx.currentPeriod
        val guide = stage?.let { GrowthGuide.forStage(it) }
        val roadmap = RoadmapStats.summarize(x.roadmap, today)
        s.copy(
            todayEvents = StudyStats.eventsOn(today, events),
            balance = BalanceStats.report(stage, d.sessions, d.tasks, x.activities, period, today),
            periodLabel = period?.label,
            missionFocus = MissionPlanner.focus(x.goals, x.goalSteps, today),
            journeyNow = JourneyPlanner.actionable(JourneyPlanner.build(ctx.birthDate, x.journey, today), today),
            hasBirthDate = ctx.hasBirthDate,
            today = today,
            syncStatus = x.sync,
            talents = TalentEngine.talents(s.subjects, d.topics, d.grades, d.sessions).take(UiDefaults.MAX_ROWS),
            streak = StudyStats.studyStreak(d.sessions),
            roadmapDone = roadmap.done,
            roadmapTotal = roadmap.total,
            mentorCount = x.members.count { it.isMentor || it.mentorEnabled && !it.isStudent },
            parentCount = x.members.count { it.isParent },
            stage = stage,
            gradeLabel = ctx.gradeLabel,
            stageTip = guide?.let { GrowthGuide.pickForDay(it.parentTips, today) },
            stageExperience = guide?.let { GrowthGuide.pickForDay(it.experiences, DateUtils.weekStart(today)) },
            recentGrades = d.grades.take(UiDefaults.MAX_RECENT_RECORDS),
            scores = StudyStats.subjectScores(d.grades, s.subjects),
            progress = StudyStats.subjectProgress(d.topics, s.subjects),
            notes = d.notes.take(UiDefaults.MAX_NOTES),
            insights = InsightEngine.analyze(s.subjects, d.topics, d.grades, d.sessions, d.tasks, events).take(UiDefaults.MAX_ROWS),
        )
    }.asUiState(viewModelScope, ParentDashboardUiState())

    fun addNote(text: String) = viewModelScope.launch { if (text.isNotBlank()) notes.add(text) }
    fun deleteNote(id: String) = viewModelScope.launch { notes.delete(id) }

    /** 학부모가 자녀에게 할 일을 배정합니다. */
    fun assignTask(title: String, subjectId: String?, type: TaskType, due: LocalDate, createdByRole: String) = viewModelScope.launch {
        tasks.save(TaskEntity(familyId = "", subjectId = subjectId, title = title, type = type, dueDate = due.toEpochDay(), createdByRole = createdByRole))
    }

    private data class Side(
        val events: List<EventEntity>,
        val sync: SyncStatus,
        val roadmap: List<RoadmapItemEntity>,
        val members: List<MemberEntity>,
        val journey: List<JourneyItemEntity>,
        val activities: List<ActivityEntity> = emptyList(),
        val goals: List<GoalEntity> = emptyList(),
        val goalSteps: List<GoalStepEntity> = emptyList(),
    )

    private data class Extra(
        val grades: List<GradeEntity>,
        val topics: List<TopicEntity>,
        val notes: List<NoteEntity>,
        val sessions: List<StudySessionEntity>,
        val tasks: List<TaskEntity>,
    )

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: ParentDashboardEvent) {
        when (event) {
            is ParentDashboardEvent.AddNote -> addNote(event.text)
            is ParentDashboardEvent.DeleteNote -> deleteNote(event.id)
            is ParentDashboardEvent.AssignTask -> assignTask(event.title, event.subjectId, event.type, event.due, event.createdByRole)
        }
    }

}
