package com.nextstep.app.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.ActivityRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.journey.ActivitySummary
import com.nextstep.app.domain.journey.PeriodCalendar
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** 활동 기록(취미·동아리·현장학습·체험). 구간별로 묶어 보여 주고 저장·삭제합니다. */
class ActivitiesViewModel(
    private val streams: FamilyDataStreams,
    private val activities: ActivityRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val filter = MutableStateFlow<ActivityType?>(null)

    val state: StateFlow<ActivitiesUiState> = combine(streams.profile, streams.members, streams.activities, filter) { profile, members, stored, filter ->
        val day = today()
        val birthDate = members.firstOrNull { it.role == Role.STUDENT.name }?.birthDate?.let { LocalDate.ofEpochDay(it) }
        val periods = birthDate?.let { PeriodCalendar.periods(it) }.orEmpty()
        val current = PeriodCalendar.periodOf(periods, day)
        val live = ActivitySummary.live(stored)
        ActivitiesUiState(
            studentName = profile.studentName,
            today = day,
            periods = periods,
            currentPeriodKey = current?.key,
            activities = live,
            countByType = ActivitySummary.countByType(live),
            ongoing = ActivitySummary.ongoing(live),
            currentPeriodCount = ActivitySummary.countInPeriod(live, current),
            filter = filter,
            loaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivitiesUiState())

    fun save(activity: ActivityEntity) = viewModelScope.launch { activities.save(activity) }
    fun delete(id: String) = viewModelScope.launch { activities.delete(id) }
    fun setFilter(type: ActivityType?) { filter.value = type }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: ActivitiesEvent) {
        when (event) {
            is ActivitiesEvent.Save -> save(event.activity)
            is ActivitiesEvent.Delete -> delete(event.id)
            is ActivitiesEvent.SetFilter -> setFilter(event.type)
        }
    }
}
