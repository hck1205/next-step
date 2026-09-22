package com.nextstep.app.ui.mentor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.GradeEntity
import com.nextstep.app.data.local.MemberEntity
import com.nextstep.app.data.local.NoteEntity
import com.nextstep.app.data.local.StudySessionEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.local.TopicEntity
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.Insight
import com.nextstep.app.domain.InsightEngine
import com.nextstep.app.domain.StudyStats
import com.nextstep.app.domain.SubjectMinutes
import com.nextstep.app.domain.SubjectProgress
import com.nextstep.app.domain.SubjectScore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 멘토 대시보드 상태. 멘토가 담당 과목을 지정했으면 모든 지표를 그 과목으로 좁혀 보여줍니다.
 */
data class MentorUiState(
    val me: MemberEntity? = null,
    val studentName: String = "",
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val allSubjects: List<SubjectEntity> = emptyList(),
    /** 담당 과목 (미지정이면 전 과목). */
    val subjects: List<SubjectEntity> = emptyList(),
    val otherMentors: List<MemberEntity> = emptyList(),
    val weekMinutes: Int = 0,
    val weeklyBySubject: List<SubjectMinutes> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val recentGrades: List<GradeEntity> = emptyList(),
    val myTasks: List<TaskEntity> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val notes: List<NoteEntity> = emptyList(),
    val roadmapTotal: Int = 0,
    val roadmapInProgress: Int = 0,
    val roadmapDone: Int = 0,
    val roadmapOverdue: Int = 0,
) {
    val needsSubjectSetup: Boolean get() = me != null && me.subjectIdList.isEmpty() && allSubjects.isNotEmpty()
}

class MentorDashboardViewModel(private val repository: StudyRepository) : ViewModel() {

    private val core = combine(repository.profile, repository.myMember, repository.members, repository.subjects, repository.sync.status) { profile, me, members, subjects, sync ->
        val mine = if (me == null || me.subjectIdList.isEmpty()) subjects else subjects.filter { it.id in me.subjectIdList }
        MentorUiState(
            me = me,
            studentName = profile.studentName,
            syncStatus = sync,
            allSubjects = subjects,
            subjects = mine,
            otherMentors = members.filter { it.role == "MENTOR" && it.id != me?.id },
        )
    }

    private val data = combine(repository.topics, repository.grades, repository.sessions, repository.tasks, repository.events) { t, g, s, ta, e -> Data(t, g, s, ta, e) }

    val state: StateFlow<MentorUiState> = combine(core, data, repository.notes, repository.roadmap) { s, d, notes, roadmap ->
        val today = com.nextstep.app.domain.DateUtils.today().toEpochDay()
        val subjectIds = s.subjects.map { it.id }.toSet()
        val grades = d.grades.filter { it.subjectId in subjectIds }
        val sessions = d.sessions.filter { it.subjectId in subjectIds }
        val topics = d.topics.filter { it.subjectId in subjectIds }
        val myId = s.me?.id
        s.copy(
            weekMinutes = StudyStats.weekMinutes(sessions),
            weeklyBySubject = StudyStats.weeklyMinutesBySubject(sessions, s.subjects).filter { it.subject != null },
            progress = StudyStats.subjectProgress(topics, s.subjects),
            scores = StudyStats.subjectScores(grades, s.subjects),
            recentGrades = grades.take(5),
            myTasks = d.tasks.filter { !it.done && it.createdByRole == "MENTOR" && (it.subjectId == null || it.subjectId in subjectIds) },
            insights = InsightEngine.analyze(s.subjects, topics, grades, sessions, d.tasks.filter { it.subjectId == null || it.subjectId in subjectIds }, d.events).take(4),
            notes = notes.filter { it.authorRole != "MENTOR" || myId == null || it.authorName == s.me?.name }.take(10),
            roadmapTotal = roadmap.size,
            roadmapInProgress = roadmap.count { it.status == com.nextstep.app.data.model.RoadmapStatus.IN_PROGRESS },
            roadmapDone = roadmap.count { it.status == com.nextstep.app.data.model.RoadmapStatus.DONE },
            roadmapOverdue = roadmap.count { it.status != com.nextstep.app.data.model.RoadmapStatus.DONE && (it.targetDate ?: Long.MAX_VALUE) < today },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MentorUiState())

    fun setSubjects(ids: List<String>) = viewModelScope.launch {
        val me = state.value.me ?: return@launch
        repository.setMemberSubjects(me.id, ids)
    }

    fun assignTask(title: String, subjectId: String?, type: TaskType, due: LocalDate) = viewModelScope.launch {
        repository.saveTask(TaskEntity(familyId = "", subjectId = subjectId, title = title, type = type, dueDate = due.toEpochDay(), createdByRole = "MENTOR"))
    }

    fun deleteTask(id: String) = viewModelScope.launch { repository.deleteTask(id) }
    fun addNote(text: String) = viewModelScope.launch { if (text.isNotBlank()) repository.addNote(text) }
    fun deleteNote(id: String) = viewModelScope.launch { repository.deleteNote(id) }

    private data class Data(
        val topics: List<TopicEntity>,
        val grades: List<GradeEntity>,
        val sessions: List<StudySessionEntity>,
        val tasks: List<TaskEntity>,
        val events: List<com.nextstep.app.data.local.EventEntity>,
    )
}
