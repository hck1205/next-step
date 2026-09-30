package com.nextstep.app.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.model.Role
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.LessonRepository
import com.nextstep.app.data.repository.MemberRepository
import com.nextstep.app.domain.access.Capabilities
import com.nextstep.app.domain.lesson.LessonPlan
import com.nextstep.app.domain.lesson.Lessons
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** 수업·출결·수업료: 학부모는 멘토마다 한 권씩 보고, 멘토는 자기 수업을 적습니다(스트림이 이미 자기 기록만 넘김). */
class LessonsViewModel(
    private val streams: FamilyDataStreams,
    private val members: MemberRepository,
    private val lessons: LessonRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.from(today()))

    val state: StateFlow<LessonsUiState> = combine(month, streams.profile, streams.myMember, streams.members, streams.lessons) { ym, profile, me, all, records ->
        val caps = Capabilities.of(profile.role ?: Role.STUDENT, me)
        LessonsUiState(
            month = ym,
            books = Lessons.booksFor(caps.canKeepLessons, me, all, records, ym, today()),
            myPlan = me?.let(LessonPlan::of) ?: LessonPlan.NONE,
            loaded = true,
        )
    }.asUiState(viewModelScope, LessonsUiState(month = YearMonth.from(today())))

    fun onEvent(event: LessonsEvent) {
        when (event) {
            is LessonsEvent.MoveMonth -> month.value = month.value.plusMonths(event.by)
            is LessonsEvent.Mark -> viewModelScope.launch { lessons.mark(event.date, event.status) }
            is LessonsEvent.SavePlan -> viewModelScope.launch { streams.myMember.first()?.let { members.setLessonPlan(it.id, event.plan) } }
        }
    }
}
