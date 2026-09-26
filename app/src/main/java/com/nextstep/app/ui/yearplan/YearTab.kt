package com.nextstep.app.ui.yearplan

import com.nextstep.app.domain.year.YearArea
import com.nextstep.app.domain.year.YearTerm

/**
 * "올해" 화면의 탭 하나. [area] 가 null 이면 전체. 기본은 때(지금 학기 → 1년 내내 → 다른 학기) 순서로 묶고(안 한 것이 먼저),
 * 그 아래 [ahead](앞서 가기)를 따로 둡니다. [done]·[total] 은 기본만 셉니다.
 */
data class YearTab(
    val area: YearArea?,
    val label: String,
    val done: Int,
    val total: Int,
    val sections: List<Pair<YearTerm, List<YearTaskView>>>,
    val ahead: List<YearTaskView> = emptyList(),
)
