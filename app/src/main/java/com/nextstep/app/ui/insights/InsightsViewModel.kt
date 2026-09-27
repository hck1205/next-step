package com.nextstep.app.ui.insights

import com.nextstep.app.domain.task.TaskDrafts
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.insight.InsightAction
import com.nextstep.app.domain.insight.InsightEngine
import com.nextstep.app.domain.insight.TalentEngine
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.ui.common.asUiState

class InsightsViewModel(
    private val streams: FamilyDataStreams,
    private val tasks: TaskRepository,
) : ViewModel() {

    private val a = combine(streams.subjects, streams.topics, streams.grades) { s, t, g -> Triple(s, t, g) }
    private val b = combine(streams.sessions, streams.tasks, streams.events) { s, t, e -> Triple(s, t, e) }

    val state: StateFlow<InsightsUiState> = combine(a, b) { (subjects, topics, grades), (sessions, tasks, events) ->
        val scores = StudyStats.subjectScores(grades, subjects)
        InsightsUiState(
            subjects = subjects,
            insights = InsightEngine.analyze(subjects, topics, grades, sessions, tasks, events),
            scores = scores,
            reviewRatios = StudyStats.subjectProgress(topics, subjects).filter { p -> scores.any { it.subject.id == p.subject.id } }.map { it.myRatio },
            daily14 = StudyStats.dailyMinutes(sessions, 14),
            weeklyBySubject = StudyStats.weeklyMinutesBySubject(sessions, subjects),
            byHour = StudyStats.minutesByHour(sessions),
            totalMinutes = sessions.sumOf { it.durationMinutes },
            talents = TalentEngine.talents(subjects, topics, grades, sessions),
        )
    }.asUiState(viewModelScope, InsightsUiState())

    fun applyAction(action: InsightAction, createdByRole: String) {
        viewModelScope.launch {
            when (action) {
                is InsightAction.CreateTask -> tasks.save(TaskDrafts.forInsight(action, createdByRole, DateUtils.today()))
            }
        }
    }


    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: InsightsEvent) {
        when (event) {
            is InsightsEvent.ApplyAction -> applyAction(event.action, event.createdByRole)
        }
    }

}
