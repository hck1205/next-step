package com.nextstep.app.ui.yearplan

import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.growth.YearProfile
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.domain.year.YearTerm
import com.nextstep.app.domain.year.YearTrend
import java.time.LocalDate

/** "올해" 탭: 올해 프로필과 분류별 탭(할 일을 잘게 나눈 목록), 지금 학기. */
data class YearPlanUiState(
    val year: YearProfile? = null,
    val level: StudentUiLevel = StudentUiLevel.TREE,
    val tabs: List<YearTab> = emptyList(),
    val currentTerm: YearTerm = YearTerm.FIRST,
    /** 기본의 끝낸 수와 전체(진행률은 기본만). */
    val done: Int = 0,
    val total: Int = 0,
    /** 앞서 가기의 끝낸 수와 전체, 묶음 이름·안내(학령 전은 "더 해 보면 좋은 것"). */
    val aheadDone: Int = 0,
    val aheadTotal: Int = 0,
    val aheadHeading: String = "",
    val aheadNote: String = "",
    val today: LocalDate = DateUtils.today(),
    /** 이 나이의 한국 교육열과 권장 기준(전체 탭 맨 위). */
    val trend: YearTrend? = null,
    /** 학령 전은 모든 줄에 "누가"를 붙입니다. 학교부터는 "스스로"가 아닌 줄에만. */
    val showsAllDoers: Boolean = false,
    /** 지금 "내 할 일"만 보는지(내 몫이 하나도 없으면 전체로 보여 주고 false). */
    val mineOnly: Boolean = false,
    /** 내 몫과 전체의 기본 개수(앞서 가기 제외). 내 몫을 모르면(화면이 아직 안 알림) [mineCount] 는 0. */
    val mineCount: Int = 0,
    val allCount: Int = 0,
    val loaded: Boolean = false,
)
