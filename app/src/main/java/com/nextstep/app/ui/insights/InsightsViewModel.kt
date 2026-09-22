package com.nextstep.app.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.NoteEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.domain.DayMinutes
import com.nextstep.app.domain.Insight
import com.nextstep.app.domain.InsightAction
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

data class InsightsUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val insights: List<Insight> = emptyList(),
    val scores: List<SubjectScore> = emptyList(),
    val progress: List<SubjectProgress> = emptyList(),
    val daily14: List<DayMinutes> = emptyList(),
    val weeklyBySubject: List<SubjectMinutes> = emptyList(),
    val byHour: IntArray = IntArray(24),
    val notes: List<NoteEntity> = emptyList(),
    val totalMinutes: Int = 0,
)

class InsightsViewModel(private val repository: StudyRepository) : ViewModel() {

    private val a = combine(repository.subjects, repository.topics, repository.grades) { s, t, g -> Triple(s, t, g) }
    private val b = combine(repository.sessions, repository.tasks, repository.events, repository.notes) { s, t, e, n -> Quad(s, t, e, n) }

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
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightsUiState())

    fun applyAction(action: InsightAction) = viewModelScope.launch {
        when (action) {
            is InsightAction.CreateTask -> repository.saveTask(
                TaskEntity(
                    familyId = "", subjectId = action.subjectId, topicId = action.topicId, title = action.title, type = action.type,
                    dueDate = DateUtils.today().plusDays(1).toEpochDay(), createdByRole = "STUDENT",
                ),
            )
        }
    }

    fun addNote(text: String) = viewModelScope.launch { if (text.isNotBlank()) repository.addNote(text) }
    fun deleteNote(id: String) = viewModelScope.launch { repository.deleteNote(id) }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}
