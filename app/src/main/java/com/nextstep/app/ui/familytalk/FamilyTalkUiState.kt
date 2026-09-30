package com.nextstep.app.ui.familytalk

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.domain.familytalk.WeekHighlights
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

data class FamilyTalkUiState(
    /** 이야기하는 주의 월요일(FamilyTalk.talkWeek). */
    val week: LocalDate = DateUtils.weekStart(DateUtils.today()),
    val today: LocalDate = DateUtils.today(),
    val highlights: WeekHighlights = WeekHighlights(),
    /** "가장 자랑하고 싶은 것" 고르기 칩. */
    val proudIdeas: List<String> = emptyList(),
    /** 다음 주에 기대되는 가족 일정. */
    val lookForward: List<FamilyOccurrence> = emptyList(),
    val members: List<MemberEntity> = emptyList(),
    val studentName: String = "",
    /** 그 주에 이미 나눈 이야기(있으면 요약을 먼저 보여 주고, 고칠 때 채워 둠). */
    val saved: WeekPlanEntity? = null,
    /** 지난 이야기(가까운 주부터). */
    val past: List<WeekPlanEntity> = emptyList(),
    val loaded: Boolean = false,
) {
    val talked: Boolean get() = saved?.talkAt != null
}
