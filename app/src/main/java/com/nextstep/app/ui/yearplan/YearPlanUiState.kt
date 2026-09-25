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
    val done: Int = 0,
    val total: Int = 0,
    val today: LocalDate = DateUtils.today(),
    /** 이 나이의 한국 교육열과 권장 기준(전체 탭 맨 위). */
    val trend: YearTrend? = null,
    /** 부모가 하는 일이 있는 해(학령 전)는 모든 줄에 "누가"를 붙입니다. 그 뒤로는 "스스로"가 아닌 줄에만. */
    val showsAllDoers: Boolean = false,
    val loaded: Boolean = false,
)
