package com.nextstep.app.ui.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.GradeEntity
import com.nextstep.app.data.local.NoteEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.DayMinutes
import com.nextstep.app.domain.Insight
import com.nextstep.app.domain.InsightEngine
import com.nextstep.app.domain.StudyStats
import com.nextstep.app.domain.SubjectMinutes
import com.nextstep.app.domain.SubjectProgress
import com.nextstep.app.domain.SubjectScore
import com.nextstep.app.domain.Talent
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.domain.UpcomingExam
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ParentUiState(
    val parentName: String = "",
    val studentName: String = "",
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val subjects: List<SubjectEntity> = emptyList(),
    val todayMinutes: Int = 0,
    val weekMinutes: Int = 0,
    val weekGoalMinutes: Int = 0,
    val daily: List<DayMinutes> = emptyList(),
    val weeklyBySubject: List<SubjectMinutes> = emptyList(),
    val pendingTasks: List<TaskEntity> = emptyList(),
    val overdueCount: Int = 0,
    val upcomingExams: List<UpcomingExam> = emptyList(),
    val recentGrades: List<GradeEntity> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val talents: List<Talent> = emptyList(),
    val streak: Int = 0,
    val roadmapDone: Int = 0,
    val roadmapTotal: Int = 0,
    val mentorCount: Int = 0,
    val parentCount: Int = 0,
)

class ParentDashboardViewModel(private val repository: StudyRepository) : ViewModel() {

    private val core = combine(repository.profile, repository.subjects, repository.sessions, repository.tasks, repository.events) { profile, subjects, sessions, tasks, events ->
        ParentUiState(
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

    private val data = combine(repository.grades, repository.topics, repository.notes, repository.sessions, repository.tasks) { grades, topics, notes, sessions, tasks ->
        Extra(grades, topics, notes, sessions, tasks)
    }

    private val extra = combine(repository.events, repository.sync.status, repository.roadmap, repository.members) { e, s, r, m -> Side(e, s, r, m) }

    val state: StateFlow<ParentUiState> = combine(core, data, extra) { s, d, x ->
        val events = x.events
        s.copy(
            syncStatus = x.sync,
            talents = InsightEngine.talents(s.subjects, d.topics, d.grades, d.sessions).take(3),
            streak = StudyStats.studyStreak(d.sessions),
            roadmapDone = x.roadmap.count { it.status == RoadmapStatus.DONE },
            roadmapTotal = x.roadmap.size,
            mentorCount = x.members.count { it.role == "MENTOR" || it.mentorEnabled && it.role != "STUDENT" },
            parentCount = x.members.count { it.role == "PARENT" },
            recentGrades = d.grades.take(5),
            scores = StudyStats.subjectScores(d.grades, s.subjects),
            progress = StudyStats.subjectProgress(d.topics, s.subjects),
            notes = d.notes.take(10),
            insights = InsightEngine.analyze(s.subjects, d.topics, d.grades, d.sessions, d.tasks, events).take(3),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ParentUiState())

    fun addNote(text: String) = viewModelScope.launch { if (text.isNotBlank()) repository.addNote(text) }
    fun deleteNote(id: String) = viewModelScope.launch { repository.deleteNote(id) }

    /** 학부모가 자녀에게 할 일을 배정합니다. */
    fun assignTask(title: String, subjectId: String?, type: TaskType, due: LocalDate, createdByRole: String) = viewModelScope.launch {
        repository.saveTask(TaskEntity(familyId = "", subjectId = subjectId, title = title, type = type, dueDate = due.toEpochDay(), createdByRole = createdByRole))
    }

    private data class Side(
        val events: List<com.nextstep.app.data.local.EventEntity>,
        val sync: SyncStatus,
        val roadmap: List<com.nextstep.app.data.local.RoadmapItemEntity>,
        val members: List<com.nextstep.app.data.local.MemberEntity>,
    )

    private data class Extra(
        val grades: List<GradeEntity>,
        val topics: List<com.nextstep.app.data.local.TopicEntity>,
        val notes: List<NoteEntity>,
        val sessions: List<com.nextstep.app.data.local.StudySessionEntity>,
        val tasks: List<TaskEntity>,
    )
}
