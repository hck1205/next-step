package com.nextstep.app.ui.mentor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.growth.GrowthGuide
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.insight.InsightEngine
import com.nextstep.app.domain.mentor.MentorScope
import com.nextstep.app.domain.stats.RoadmapStats
import com.nextstep.app.domain.stats.ScoreStats
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.UiDefaults
import com.nextstep.app.ui.common.asUiState
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** 멘토 대시보드. 담당 과목 범위(MentorScope)로 모든 지표를 좁힙니다. */
class MentorDashboardViewModel(
    private val streams: FamilyDataStreams,
    private val members: MemberRepository,
    private val tasks: TaskRepository,
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

    val state: StateFlow<MentorDashboardUiState> = combine(core, data, streams.roadmap) { s, d, roadmap ->
        val scope = MentorScope(s.subjects)
        val grades = scope.own(d.grades) { it.subjectId }
        val sessions = scope.own(d.sessions) { it.subjectId }
        val topics = scope.own(d.topics) { it.subjectId }
        val scopedTasks = scope.ownOrGeneral(d.tasks) { it.subjectId }
        val scores = StudyStats.subjectScores(grades, s.subjects)
        s.copy(
            weekMinutes = StudyStats.weekMinutes(sessions),
            weeklyBySubject = StudyStats.weeklyMinutesBySubject(sessions, s.subjects).filter { it.subject != null },
            progress = StudyStats.subjectProgress(topics, s.subjects),
            scores = scores,
            averageScore = ScoreStats.overallAverage(scores),
            recentGrades = grades.take(UiDefaults.MAX_RECENT_RECORDS),
            myTasks = scopedTasks.filter { !it.done && it.createdByRole == Role.MENTOR.name },
            insights = InsightEngine.analyze(s.subjects, topics, grades, sessions, scopedTasks, d.events).take(UiDefaults.MAX_INSIGHTS),
            roadmap = RoadmapStats.summarize(roadmap, DateUtils.today()),
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
            is MentorDashboardEvent.AssignTask -> assignTask(event.title, event.subjectId, event.type, event.due)
            is MentorDashboardEvent.DeleteTask -> deleteTask(event.id)
        }
    }
}
