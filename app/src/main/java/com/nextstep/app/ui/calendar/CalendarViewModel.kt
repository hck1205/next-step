package com.nextstep.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.EventEntity
import com.nextstep.app.data.local.StudySessionEntity
import com.nextstep.app.data.local.SubjectEntity
import com.nextstep.app.data.local.TaskEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.StudyRepository
import com.nextstep.app.domain.DateUtils
import com.nextstep.app.domain.EventOccurrence
import com.nextstep.app.domain.StudyStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

data class DayMarker(val hasEvent: Boolean, val hasExam: Boolean, val hasTask: Boolean, val studyMinutes: Int)

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selected: LocalDate = LocalDate.now(),
    val subjects: List<SubjectEntity> = emptyList(),
    val markers: Map<LocalDate, DayMarker> = emptyMap(),
    val dayEvents: List<EventOccurrence> = emptyList(),
    val dayTasks: List<TaskEntity> = emptyList(),
    val daySessions: List<StudySessionEntity> = emptyList(),
    val dayMinutes: Int = 0,
)

class CalendarViewModel(private val repository: StudyRepository) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val selected = MutableStateFlow(LocalDate.now())

    val state: StateFlow<CalendarUiState> = combine(month, selected, repository.subjects, repository.events, combine(repository.tasks, repository.sessions) { t, s -> t to s }) { m, sel, subjects, events, (tasks, sessions) ->
        val markers = buildMap {
            for (day in 1..m.lengthOfMonth()) {
                val d = m.atDay(day)
                val evs = StudyStats.eventsOn(d, events)
                val dayTasks = tasks.filter { !it.done && it.dueDate == d.toEpochDay() }
                val minutes = StudyStats.minutesBetween(sessions, d, d.plusDays(1))
                if (evs.isNotEmpty() || dayTasks.isNotEmpty() || minutes > 0) {
                    put(d, DayMarker(evs.isNotEmpty(), evs.any { it.event.type == EventType.EXAM }, dayTasks.isNotEmpty(), minutes))
                }
            }
        }
        val dayStart = DateUtils.startOfDayMillis(sel)
        val dayEnd = DateUtils.startOfDayMillis(sel.plusDays(1))
        CalendarUiState(
            month = m, selected = sel, subjects = subjects, markers = markers,
            dayEvents = StudyStats.eventsOn(sel, events),
            dayTasks = tasks.filter { it.dueDate == sel.toEpochDay() },
            daySessions = sessions.filter { it.startAt in dayStart until dayEnd },
            dayMinutes = StudyStats.minutesBetween(sessions, sel, sel.plusDays(1)),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun prevMonth() { month.value = month.value.minusMonths(1) }
    fun nextMonth() { month.value = month.value.plusMonths(1) }
    fun select(date: LocalDate) { selected.value = date; month.value = YearMonth.from(date) }
    fun today() = select(LocalDate.now())

    fun saveEvent(existing: EventEntity?, title: String, subjectId: String?, type: EventType, date: LocalDate, start: LocalTime, end: LocalTime, repeatWeekly: Boolean, location: String, memo: String) = viewModelScope.launch {
        val startMs = DateUtils.toMillis(date, start)
        val endMs = DateUtils.toMillis(date, if (end.isAfter(start)) end else start.plusHours(1))
        val event = (existing ?: EventEntity(familyId = "", title = title, startAt = startMs, endAt = endMs)).copy(
            title = title, subjectId = subjectId, type = type, startAt = startMs, endAt = endMs, repeatWeekly = repeatWeekly, location = location, memo = memo,
        )
        repository.saveEvent(event)
    }

    fun deleteEvent(id: String) = viewModelScope.launch { repository.deleteEvent(id) }

    fun saveTask(existing: TaskEntity?, title: String, subjectId: String?, type: TaskType, due: LocalDate, role: String) = viewModelScope.launch {
        val task = (existing ?: TaskEntity(familyId = "", title = title, dueDate = due.toEpochDay(), createdByRole = role)).copy(
            title = title, subjectId = subjectId, type = type, dueDate = due.toEpochDay(),
        )
        repository.saveTask(task)
    }

    fun toggleTask(task: TaskEntity) = viewModelScope.launch { repository.setTaskDone(task.id, !task.done) }
    fun deleteTask(id: String) = viewModelScope.launch { repository.deleteTask(id) }
}
