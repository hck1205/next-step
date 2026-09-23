package com.nextstep.app.ui.calendar

import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.stats.EventOccurrence
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selected: LocalDate = DateUtils.today(),
    val subjects: List<SubjectEntity> = emptyList(),
    val markers: Map<LocalDate, DayMarker> = emptyMap(),
    val dayEvents: List<EventOccurrence> = emptyList(),
    val dayTasks: List<TaskEntity> = emptyList(),
    val daySessions: List<StudySessionEntity> = emptyList(),
    val dayMinutes: Int = 0,
)
