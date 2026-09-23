package com.nextstep.app.ui.mentor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.NoteRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.insight.InsightEngine
import com.nextstep.app.domain.stats.StudyStats
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.domain.growth.GrowthGuide
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.data.model.Role
import com.nextstep.app.ui.common.asUiState

class MentorDashboardViewModel(
    private val streams: FamilyDataStreams,
    private val members: MemberRepository,
    private val tasks: TaskRepository,
    private val notes: NoteRepository,
) : ViewModel() {

    private val core = combine(streams.profile, streams.myMember, streams.members, streams.subjects, streams.syncStatus) { profile, me, members, subjects, sync ->
        val mine = if (me == null || me.subjectIdList.isEmpty()) subjects else subjects.filter { it.id in me.subjectIdList }
        MentorDashboardUiState(
            me = me,
            studentName = profile.studentName,
            syncStatus = sync,
            allSubjects = subjects,
            subjects = mine,
            otherMentors = members.filter { it.isMentor && it.id != me?.id },
            stage = GrowthStage.of(members),
            mentorTip = GrowthStage.of(members)?.let { GrowthGuide.pickForDay(GrowthGuide.forStage(it).mentorTips, DateUtils.today()) },
        )
    }

    private val data = combine(streams.topics, streams.grades, streams.sessions, streams.tasks, streams.events) { t, g, s, ta, e -> Data(t, g, s, ta, e) }

    val state: StateFlow<MentorDashboardUiState> = combine(core, data, streams.notes, streams.roadmap) { s, d, notes, roadmap ->
        val today = com.nextstep.app.domain.time.DateUtils.today().toEpochDay()
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
            myTasks = d.tasks.filter { !it.done && it.createdByRole == Role.MENTOR.name && (it.subjectId == null || it.subjectId in subjectIds) },
            insights = InsightEngine.analyze(s.subjects, topics, grades, sessions, d.tasks.filter { it.subjectId == null || it.subjectId in subjectIds }, d.events).take(4),
            notes = notes.filter { it.authorRole != Role.MENTOR.name || myId == null || it.authorName == s.me?.name }.take(10),
            roadmapTotal = roadmap.size,
            roadmapInProgress = roadmap.count { it.status == com.nextstep.app.data.model.RoadmapStatus.IN_PROGRESS },
            roadmapDone = roadmap.count { it.status == com.nextstep.app.data.model.RoadmapStatus.DONE },
            roadmapOverdue = roadmap.count { it.status != com.nextstep.app.data.model.RoadmapStatus.DONE && (it.targetDate ?: Long.MAX_VALUE) < today },
        )
    }.asUiState(viewModelScope, MentorDashboardUiState())

    fun setSubjects(ids: List<String>) = viewModelScope.launch {
        val me = state.value.me ?: return@launch
        members.setSubjects(me.id, ids)
    }

    fun assignTask(title: String, subjectId: String?, type: TaskType, due: LocalDate) = viewModelScope.launch {
        tasks.save(TaskEntity(familyId = "", subjectId = subjectId, title = title, type = type, dueDate = due.toEpochDay(), createdByRole = Role.MENTOR.name))
    }

    fun deleteTask(id: String) = viewModelScope.launch { tasks.delete(id) }
    fun addNote(text: String) = viewModelScope.launch { if (text.isNotBlank()) notes.add(text) }
    fun deleteNote(id: String) = viewModelScope.launch { notes.delete(id) }

    private data class Data(
        val topics: List<TopicEntity>,
        val grades: List<GradeEntity>,
        val sessions: List<StudySessionEntity>,
        val tasks: List<TaskEntity>,
        val events: List<com.nextstep.app.data.local.entity.EventEntity>,
    )

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: MentorDashboardEvent) {
        when (event) {
            is MentorDashboardEvent.SetSubjects -> setSubjects(event.ids)
            is MentorDashboardEvent.AssignTask -> assignTask(event.title, event.subjectId, event.type, event.due)
            is MentorDashboardEvent.DeleteTask -> deleteTask(event.id)
            is MentorDashboardEvent.AddNote -> addNote(event.text)
            is MentorDashboardEvent.DeleteNote -> deleteNote(event.id)
        }
    }

}
