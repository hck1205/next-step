package com.nextstep.app.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.TaskType
import com.nextstep.app.data.repository.EventRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.nextstep.app.ui.common.asUiState

class CalendarViewModel(
    private val streams: FamilyDataStreams,
    private val events: EventRepository,
    private val tasks: TaskRepository,
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val selected = MutableStateFlow(LocalDate.now())

    val state: StateFlow<CalendarUiState> = combine(month, selected, streams.subjects, streams.events, combine(streams.tasks, streams.sessions) { t, s -> t to s }) { m, sel, subjects, events, (tasks, sessions) ->
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
    }.asUiState(viewModelScope, CalendarUiState())

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
        events.save(event)
    }

    fun deleteEvent(id: String) = viewModelScope.launch { events.delete(id) }

    fun saveTask(existing: TaskEntity?, title: String, subjectId: String?, type: TaskType, due: LocalDate, role: String) = viewModelScope.launch {
        val task = (existing ?: TaskEntity(familyId = "", title = title, dueDate = due.toEpochDay(), createdByRole = role)).copy(
            title = title, subjectId = subjectId, type = type, dueDate = due.toEpochDay(),
        )
        tasks.save(task)
    }

    fun toggleTask(task: TaskEntity) = viewModelScope.launch { tasks.setDone(task.id, !task.done) }
    fun deleteTask(id: String) = viewModelScope.launch { tasks.delete(id) }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: CalendarEvent) {
        when (event) {
            CalendarEvent.PrevMonth -> prevMonth()
            CalendarEvent.NextMonth -> nextMonth()
            is CalendarEvent.Select -> select(event.date)
            CalendarEvent.Today -> today()
            is CalendarEvent.SaveEvent -> saveEvent(event.existing, event.title, event.subjectId, event.type, event.date, event.start, event.end, event.repeatWeekly, event.location, event.memo)
            is CalendarEvent.DeleteEvent -> deleteEvent(event.id)
            is CalendarEvent.SaveTask -> saveTask(event.existing, event.title, event.subjectId, event.type, event.due, event.role)
            is CalendarEvent.ToggleTask -> toggleTask(event.task)
            is CalendarEvent.DeleteTask -> deleteTask(event.id)
        }
    }

}
