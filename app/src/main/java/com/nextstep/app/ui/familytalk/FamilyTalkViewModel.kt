package com.nextstep.app.ui.familytalk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.repository.FamilyDataStreams
import com.nextstep.app.data.repository.WeekPlanRepository
import com.nextstep.app.domain.family.student
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.familytalk.FamilyTalk
import com.nextstep.app.domain.feedback.FeedbackEngine
import com.nextstep.app.domain.growth.StudentScreen
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.common.asUiState
import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 주말 이야기: 이야기할 주(금~일이면 이번 주, 월~목이면 지난주)의 반짝인 순간을 모으고, 다음 주 기대되는 가족 일정을 보여 주고,
 * 가족이 고른 자랑 · 해 보고 싶은 것 · 가족 즐거움을 그 주의 주간 계획 행에 남깁니다. 좋았던 것만 모읍니다.
 */
class FamilyTalkViewModel(
    streams: FamilyDataStreams,
    private val weekPlans: WeekPlanRepository,
    private val today: () -> LocalDate = { DateUtils.today() },
) : ViewModel() {
    private val done = combine(streams.tasks, streams.sessions, streams.cheers, streams.goals, streams.members) { t, s, c, g, m -> Done(t, s, c, g, m) }
    private val learn = combine(streams.subjects, streams.topics, streams.grades) { s, t, g -> Triple(s, t, g) }

    val state: StateFlow<FamilyTalkUiState> = combine(done, learn, streams.familyEvents, streams.weekPlans) { d, (subjects, topics, grades), family, plans ->
        val day = today()
        val week = FamilyTalk.talkWeek(day)
        val end = minOf(week.plusDays(DAYS_IN_WEEK - 1), day)
        val student = d.members.student()
        val numbers = StudentScreen.of(student, day).level.showsNumbers
        val findings = FeedbackEngine.findings(subjects, topics, grades, d.sessions, d.tasks, end)
        val highlights = FamilyTalk.highlights(week, d.tasks, d.sessions, d.cheers, d.goals, findings, numbers)
        FamilyTalkUiState(
            week = week, today = day, highlights = highlights, proudIdeas = FamilyTalk.proudIdeas(week, d.tasks, highlights),
            lookForward = FamilyTalk.lookForward(family, week), members = FamilyCalendar.family(d.members), studentName = student?.name.orEmpty(),
            saved = plans.firstOrNull { !it.deleted && it.weekStart == week.toEpochDay() }, past = FamilyTalk.past(plans, week), loaded = true,
        )
    }.asUiState(viewModelScope, FamilyTalkUiState())

    fun onEvent(event: FamilyTalkEvent) {
        when (event) {
            is FamilyTalkEvent.Save -> viewModelScope.launch { weekPlans.saveTalk(state.value.week, event.proud, event.wish, event.treat) }
        }
    }

    private data class Done(
        val tasks: List<TaskEntity>,
        val sessions: List<StudySessionEntity>,
        val cheers: List<CheerEntity>,
        val goals: List<GoalEntity>,
        val members: List<MemberEntity>,
    )

    private companion object {
        const val DAYS_IN_WEEK = 7L
    }
}
