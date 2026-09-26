package com.nextstep.app.domain.project

/** 단계의 루틴 한 줄: 무엇을 한 번에 [minutes]분, 일주일에 [daysPerWeek]일. */
data class RoutineItem(
    val name: String,
    val kind: RoutineKind,
    val minutes: Int,
    val daysPerWeek: Int,
) {
    val weeklyMinutes: Int get() = minutes * daysPerWeek
    val amountLabel: String get() = if (daysPerWeek >= DAYS_IN_WEEK) "매일 ${minutes}분" else "주 ${daysPerWeek}일 · ${minutes}분"

    private companion object {
        const val DAYS_IN_WEEK = 7
    }
}
