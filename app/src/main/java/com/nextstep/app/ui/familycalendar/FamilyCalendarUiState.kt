package com.nextstep.app.ui.familycalendar

import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.familycalendar.FamilyOccurrence
import com.nextstep.app.domain.stats.EventOccurrence
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.YearMonth

data class FamilyCalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selected: LocalDate = DateUtils.today(),
    val today: LocalDate = DateUtils.today(),
    /** 가족 구성원(멘토 제외): 누구 고르기와 "누구의 일정" 문구에 씁니다. */
    val members: List<MemberEntity> = emptyList(),
    /** 한 사람만 볼 때 그 구성원 id. null 이면 모두. */
    val filter: String? = null,
    /** 이 달의 날마다 걸린 가족 일정(고른 사람 기준). */
    val days: Map<LocalDate, List<FamilyOccurrence>> = emptyMap(),
    val dayEvents: List<FamilyOccurrence> = emptyList(),
    /** 아이의 공부 일정(학원·시험 등, 기록 › 공부 › 일정)이 있는 날. 가족 달력에서는 읽기만 합니다. */
    val studyDays: Set<LocalDate> = emptySet(),
    val dayStudy: List<EventOccurrence> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val studentName: String = "",
    val loaded: Boolean = false,
)
