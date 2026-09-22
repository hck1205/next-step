package com.nextstep.app.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.NoteRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.insight.InsightEngine
import com.nextstep.app.domain.insight.TalentEngine
import com.nextstep.app.domain.stats.StudyStats
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.nextstep.app.domain.growth.GrowthGuide
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyPlanner
import com.nextstep.app.domain.time.DateUtils

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
            upcomingExams = StudyStats.upcomingExams(events, tasks).take(3),
        )
    }

    private val data = combine(streams.grades, streams.topics, streams.notes, streams.sessions, streams.tasks) { grades, topics, notes, sessions, tasks ->
        Extra(grades, topics, notes, sessions, tasks)
    }

    private val extra = combine(streams.events, streams.syncStatus, streams.roadmap, streams.members, streams.journeyItems) { e, s, r, m, j -> Side(e, s, r, m, j) }

    val state: StateFlow<ParentDashboardUiState> = combine(core, data, extra) { s, d, x ->
        val events = x.events
        val today = DateUtils.today()
        val birthDate = x.members.firstOrNull { it.role == "STUDENT" }?.birthDate?.let { java.time.LocalDate.ofEpochDay(it) }
        s.copy(
            journeyNow = JourneyPlanner.actionable(JourneyPlanner.build(birthDate, x.journey, today), today),
            hasBirthDate = birthDate != null,
            today = today,
            syncStatus = x.sync,
            talents = TalentEngine.talents(s.subjects, d.topics, d.grades, d.sessions).take(3),
            streak = StudyStats.studyStreak(d.sessions),
            roadmapDone = x.roadmap.count { it.status == RoadmapStatus.DONE },
            roadmapTotal = x.roadmap.size,
            mentorCount = x.members.count { it.role == "MENTOR" || it.mentorEnabled && it.role != "STUDENT" },
            parentCount = x.members.count { it.role == "PARENT" },
            stage = GrowthStage.of(x.members, today),
            gradeLabel = x.members.firstOrNull { it.role == "STUDENT" }?.gradeYear?.let { y -> GrowthStage.fromGradeYear(y)?.gradeLabel(y) },
            stageTip = GrowthStage.of(x.members)?.let { GrowthGuide.pickForDay(GrowthGuide.forStage(it).parentTips, DateUtils.today()) },
            stageExperience = GrowthStage.of(x.members)?.let { GrowthGuide.pickForDay(GrowthGuide.forStage(it).experiences, DateUtils.weekStart()) },
            recentGrades = d.grades.take(5),
            scores = StudyStats.subjectScores(d.grades, s.subjects),
            progress = StudyStats.subjectProgress(d.topics, s.subjects),
            notes = d.notes.take(10),
            insights = InsightEngine.analyze(s.subjects, d.topics, d.grades, d.sessions, d.tasks, events).take(3),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ParentDashboardUiState())

    fun addNote(text: String) = viewModelScope.launch { if (text.isNotBlank()) notes.add(text) }
    fun deleteNote(id: String) = viewModelScope.launch { notes.delete(id) }

    /** 학부모가 자녀에게 할 일을 배정합니다. */
    fun assignTask(title: String, subjectId: String?, type: TaskType, due: LocalDate, createdByRole: String) = viewModelScope.launch {
        tasks.save(TaskEntity(familyId = "", subjectId = subjectId, title = title, type = type, dueDate = due.toEpochDay(), createdByRole = createdByRole))
    }

    private data class Side(
        val events: List<com.nextstep.app.data.local.entity.EventEntity>,
        val sync: SyncStatus,
        val roadmap: List<com.nextstep.app.data.local.entity.RoadmapItemEntity>,
        val members: List<com.nextstep.app.data.local.entity.MemberEntity>,
        val journey: List<com.nextstep.app.data.local.entity.JourneyItemEntity>,
    )

    private data class Extra(
        val grades: List<GradeEntity>,
        val topics: List<com.nextstep.app.data.local.entity.TopicEntity>,
        val notes: List<NoteEntity>,
        val sessions: List<com.nextstep.app.data.local.entity.StudySessionEntity>,
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
