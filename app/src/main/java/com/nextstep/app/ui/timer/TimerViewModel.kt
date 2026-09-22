package com.nextstep.app.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.StudySessionEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.prefs.RunningTimer
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.domain.StudyStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

data class TimerUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val running: RunningTimer? = null,
    val elapsedSeconds: Long = 0,
    val todayMinutes: Int = 0,
    val todaySessions: List<StudySessionEntity> = emptyList(),
    val selectedSubjectId: String? = null,
    val lastSaved: StudySessionEntity? = null,
)

class TimerViewModel(private val repository: StudyRepository) : ViewModel() {
    private val selected = MutableStateFlow<String?>(null)
    private val tick = MutableStateFlow(0L)
    private val lastSaved = MutableStateFlow<StudySessionEntity?>(null)

    init {
        viewModelScope.launch {
            while (true) { tick.value = System.currentTimeMillis(); delay(1_000) }
        }
    }

    val state: StateFlow<TimerUiState> = combine(repository.subjects, repository.runningTimer, repository.sessions, selected, tick) { subjects, running, sessions, sel, now ->
        val todayStart = DateUtils.startOfDayMillis(DateUtils.today())
        TimerUiState(
            subjects = subjects,
            running = running,
            elapsedSeconds = running?.let { ((now - it.startedAt) / 1000).coerceAtLeast(0) } ?: 0,
            todayMinutes = StudyStats.todayMinutes(sessions),
            todaySessions = sessions.filter { it.startAt >= todayStart },
            selectedSubjectId = sel ?: running?.subjectId ?: subjects.firstOrNull()?.id,
        )
    }.combine(lastSaved) { s, saved -> s.copy(lastSaved = saved) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    fun selectSubject(id: String?) { selected.value = id }

    fun start() = viewModelScope.launch {
        lastSaved.value = null
        repository.startTimer(state.value.selectedSubjectId)
    }

    fun stop() = viewModelScope.launch { lastSaved.value = repository.stopTimer() }

    fun cancel() = viewModelScope.launch { repository.cancelTimer() }

    /** 타이머 없이 직접 기록. */
    fun addManual(subjectId: String?, date: LocalDate, start: LocalTime, minutes: Int, note: String) = viewModelScope.launch {
        if (minutes <= 0) return@launch
        val startMs = DateUtils.toMillis(date, start)
        repository.saveSession(
            StudySessionEntity(familyId = "", subjectId = subjectId, startAt = startMs, endAt = startMs + minutes * 60_000L, durationMinutes = minutes, note = note),
        )
    }

    fun delete(id: String) = viewModelScope.launch { repository.deleteSession(id) }
}
