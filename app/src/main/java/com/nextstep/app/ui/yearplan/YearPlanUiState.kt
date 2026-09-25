package com.nextstep.app.ui.yearplan

import com.nextstep.app.domain.growth.StudentUiLevel
import com.nextstep.app.domain.growth.YearProfile
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.domain.year.YearTerm
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
    val loaded: Boolean = false,
)
