package com.nextstep.app.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.data.repository.ActivityRepository
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.domain.journey.ActivitySummary
import com.nextstep.app.domain.family.StudentContext
import com.nextstep.app.domain.time.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.nextstep.app.ui.common.asUiState

/** 활동 기록(취미·동아리·현장학습·체험). 구간별로 묶어 보여 주고 저장·삭제합니다. */
class ActivitiesViewModel(
    private val streams: FamilyDataStreams,
    private val activities: ActivityRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val filter = MutableStateFlow<ActivityType?>(null)

    val state: StateFlow<ActivitiesUiState> = combine(streams.profile, streams.members, streams.activities, filter) { profile, members, stored, filter ->
        val day = today()
        val ctx = StudentContext.of(members, day)
        val live = ActivitySummary.live(stored)
        val filtered = live.filter { filter == null || it.type == filter }
        ActivitiesUiState(
            studentName = profile.studentName,
            today = day,
            periods = ctx.periods,
            currentPeriodKey = ctx.currentPeriodKey,
            activities = live,
            countByType = ActivitySummary.countByType(live),
            ongoing = ActivitySummary.ongoing(live),
            currentPeriodCount = ActivitySummary.countInPeriod(live, ctx.currentPeriod),
            filter = filter,
            loaded = true,
            sections = ActivitiesUiState.sectionsOf(filtered, ctx.periods),
        )
    }.asUiState(viewModelScope, ActivitiesUiState())

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
