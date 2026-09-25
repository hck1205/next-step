package com.nextstep.app.ui.habits

import com.nextstep.app.domain.stats.StudyHabitReport

/** 공부 › 습관: 최근 4주 공부 습관(언제·얼마나·며칠 이어서). */
data class HabitsUiState(
    val report: StudyHabitReport? = null,
    val loaded: Boolean = false,
)
