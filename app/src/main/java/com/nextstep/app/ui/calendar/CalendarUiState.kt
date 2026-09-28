package com.nextstep.app.ui.calendar

import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.domain.selfdirection.SelfDirectionStage
import com.nextstep.app.domain.stats.EventOccurrence
import com.nextstep.app.domain.time.DateUtils
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
    /** 아이의 자기주도 단계: 할 일 체크를 누가 하는지(caps.canCheckTask). */
    val stage: SelfDirectionStage = SelfDirectionStage.OWN,
)
