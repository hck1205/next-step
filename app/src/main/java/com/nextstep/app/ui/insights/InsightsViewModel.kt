package com.nextstep.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.NoteRepository
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.insight.InsightAction
import com.nextstep.app.domain.insight.InsightEngine
import com.nextstep.app.domain.insight.TalentEngine
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InsightsViewModel(
    private val streams: FamilyDataStreams,
    private val tasks: TaskRepository,
    private val notes: NoteRepository,
) : ViewModel() {

    private val a = combine(streams.subjects, streams.topics, streams.grades) { s, t, g -> Triple(s, t, g) }
    private val b = combine(streams.sessions, streams.tasks, streams.events, streams.notes) { s, t, e, n -> Quad(s, t, e, n) }

    val state: StateFlow<InsightsUiState> = combine(a, b) { (subjects, topics, grades), (sessions, tasks, events, notes) ->
        InsightsUiState(
            subjects = subjects,
            insights = InsightEngine.analyze(subjects, topics, grades, sessions, tasks, events),
            scores = StudyStats.subjectScores(grades, subjects),
            progress = StudyStats.subjectProgress(topics, subjects),
            daily14 = StudyStats.dailyMinutes(sessions, 14),
            weeklyBySubject = StudyStats.weeklyMinutesBySubject(sessions, subjects),
            byHour = StudyStats.minutesByHour(sessions),
            notes = notes,
            totalMinutes = sessions.sumOf { it.durationMinutes },
            talents = TalentEngine.talents(subjects, topics, grades, sessions),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    fun applyAction(action: InsightAction, createdByRole: String) = viewModelScope.launch {
        when (action) {
            is InsightAction.CreateTask -> tasks.save(
                TaskEntity(
                    familyId = "", subjectId = action.subjectId, topicId = action.topicId, title = action.title, type = action.type,
                    dueDate = DateUtils.today().plusDays(1).toEpochDay(), createdByRole = createdByRole,
                ),
            )
        }
    }

    fun addNote(text: String) = viewModelScope.launch { if (text.isNotBlank()) notes.add(text) }
    fun deleteNote(id: String) = viewModelScope.launch { notes.delete(id) }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: InsightsEvent) {
        when (event) {
            is InsightsEvent.ApplyAction -> applyAction(event.action, event.createdByRole)
            is InsightsEvent.AddNote -> addNote(event.text)
            is InsightsEvent.DeleteNote -> deleteNote(event.id)
        }
    }

}
