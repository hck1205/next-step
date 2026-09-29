package com.nextstep.app.ui.familycalendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.FamilyEventRepository
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.stats.StudyStats
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 가족 달력: 학생·학부모가 서로의 일정을 한 달 칸으로 보고, 날짜를 눌러 넣고 고칩니다.
 * 멘토에게는 이 화면이 없고(ConcernSection.audiences), 일정도 넘어오지 않습니다(MentorScopedStreams).
 * 아이의 공부 일정(학원·시험)은 함께 보여 주되 여기서 고치지 않습니다.
 */
class FamilyCalendarViewModel(
    streams: FamilyDataStreams,
    private val events: FamilyEventRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.from(today()))
    private val selected = MutableStateFlow(today())
    private val filter = MutableStateFlow<String?>(null)
    private val view = combine(month, selected, filter) { m, s, f -> View(m, s, f) }
    private val study = combine(streams.events, streams.subjects) { e, s -> e to s }

    val state: StateFlow<FamilyCalendarUiState> = combine(view, streams.familyEvents, streams.members, study) { v, family, members, (studyEvents, subjects) ->
        build(v, family, members, studyEvents, subjects)
    }.asUiState(viewModelScope, FamilyCalendarUiState())

    private fun build(v: View, family: List<FamilyEventEntity>, members: List<MemberEntity>, studyEvents: List<EventEntity>, subjects: List<SubjectEntity>): FamilyCalendarUiState {
        val people = FamilyCalendar.family(members)
        val who = v.filter?.takeIf { id -> people.any { it.id == id } }
        val shown = who?.let { id -> family.filter { FamilyCalendar.involves(it, id) } } ?: family
        val student = people.firstOrNull { it.isStudent }
        val withStudy = who == null || who == student?.id
        val monthDays = (1..v.month.lengthOfMonth()).map { v.month.atDay(it) }
        return FamilyCalendarUiState(
            month = v.month, selected = v.selected, today = today(), members = people, filter = who,
            days = FamilyCalendar.month(shown, v.month), dayEvents = FamilyCalendar.on(shown, v.selected),
            studyDays = if (withStudy) monthDays.filter { StudyStats.eventsOn(it, studyEvents).isNotEmpty() }.toSet() else emptySet(),
            dayStudy = if (withStudy) StudyStats.eventsOn(v.selected, studyEvents) else emptyList(),
            subjects = subjects, studentName = student?.name.orEmpty(), loaded = true,
        )
    }

    /** 화면 이벤트 단일 진입점. */
    fun onEvent(event: FamilyCalendarEvent) {
        when (event) {
            FamilyCalendarEvent.PrevMonth -> month.value = month.value.minusMonths(1)
            FamilyCalendarEvent.NextMonth -> month.value = month.value.plusMonths(1)
            FamilyCalendarEvent.Today -> select(today())
            is FamilyCalendarEvent.Select -> select(event.date)
            is FamilyCalendarEvent.Filter -> filter.value = event.memberId
            is FamilyCalendarEvent.Save -> if (event.draft.canSave) viewModelScope.launch { events.save(event.draft.toEntity(event.existing)) }
            is FamilyCalendarEvent.Delete -> viewModelScope.launch { events.delete(event.id) }
        }
    }

    private fun select(date: LocalDate) {
        selected.value = date
        month.value = YearMonth.from(date)
    }

    private data class View(val month: YearMonth, val selected: LocalDate, val filter: String?)
}
