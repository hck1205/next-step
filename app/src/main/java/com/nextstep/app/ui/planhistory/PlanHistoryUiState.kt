package com.nextstep.app.ui.planhistory

import com.nextstep.app.domain.goaltree.GoalNode
import com.nextstep.app.domain.goaltree.HistoryEvent
import com.nextstep.app.domain.goaltree.RateBy
import com.nextstep.app.domain.goaltree.WeekRate

/**
 * 기록 › 목표·할 일 › 기록: 최근 4주 달성률, 주별 달성(8주), 누가 준 할 일 · 과목별 달성률, 달성한 목표와 그 달성이 이어진 목표, 타임라인.
 */
data class PlanHistoryUiState(
    val loaded: Boolean = false,
    val recentRate: Float? = null,
    val weeks: List<WeekRate> = emptyList(),
    val byAssigner: List<RateBy> = emptyList(),
    val bySubject: List<RateBy> = emptyList(),
    /** 달성한 목표(최근 먼저)와 이어진 목표의 지금 달성률 한 줄. */
    val achieved: List<Pair<GoalNode, String?>> = emptyList(),
    val timeline: List<HistoryEvent> = emptyList(),
)
