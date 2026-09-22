package com.nextstep.app.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.StudySessionRepository
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerViewModel(
    private val streams: FamilyDataStreams,
    private val sessions: StudySessionRepository,
    tickMillis: Long = DEFAULT_TICK_MILLIS,
) : ViewModel() {
    private val selected = MutableStateFlow<String?>(null)
    private val lastSaved = MutableStateFlow<StudySessionEntity?>(null)

    /** 화면이 보고 있을 때만 흐르는 초 단위 시계. 구독이 끊기면 멈춥니다. */
    private val tick = flow {
        while (true) { emit(System.currentTimeMillis()); delay(tickMillis) }
    }

    val state: StateFlow<TimerUiState> = combine(streams.subjects, streams.runningTimer, streams.sessions, selected, tick) { subjects, running, sessions, sel, now ->
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
        sessions.startTimer(state.value.selectedSubjectId)
    }

    fun stop() = viewModelScope.launch { lastSaved.value = sessions.stopTimer() }

    fun cancel() = viewModelScope.launch { sessions.cancelTimer() }

    /** 타이머 없이 직접 기록. */
    fun addManual(subjectId: String?, date: LocalDate, start: LocalTime, minutes: Int, note: String) = viewModelScope.launch {
        if (minutes <= 0) return@launch
        val startMs = DateUtils.toMillis(date, start)
        sessions.save(
            StudySessionEntity(familyId = "", subjectId = subjectId, startAt = startMs, endAt = startMs + minutes * 60_000L, durationMinutes = minutes, note = note),
        )
    }

    fun delete(id: String) = viewModelScope.launch { sessions.delete(id) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: TimerEvent) {
        when (event) {
            is TimerEvent.SelectSubject -> selectSubject(event.id)
            TimerEvent.Start -> start()
            TimerEvent.Stop -> stop()
            TimerEvent.Cancel -> cancel()
            is TimerEvent.AddManual -> addManual(event.subjectId, event.date, event.start, event.minutes, event.note)
            is TimerEvent.Delete -> delete(event.id)
        }
    }


    private companion object {
        const val DEFAULT_TICK_MILLIS = 1_000L
    }
}
