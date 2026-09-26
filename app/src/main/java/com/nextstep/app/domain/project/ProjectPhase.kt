package com.nextstep.app.domain.project

import kotlin.math.roundToInt

/**
 * 프로젝트의 단계 하나. [fromMonths] 는 이 단계를 보통 시작하는 만 나이(개월), [weeks] 는 보통 걸리는 기간입니다.
 * [checkpoint] 를 통과하면 다음 단계로 넘어가고, 루틴의 양은 단계마다 조금씩 늘어납니다.
 * [key] 는 저장된 기록과 연결되므로 바꾸지 않습니다.
 */
data class ProjectPhase(
    val key: String,
    val title: String,
    val ageLabel: String,
    val fromMonths: Int,
    val weeks: Int,
    val routine: List<RoutineItem>,
    val checkpoint: String,
    val materials: String,
    val tip: String,
) {
    val weeklyMinutes: Int get() = routine.sumOf { it.weeklyMinutes }
    /** 하루 평균(주간 합 ÷ 7). 화면의 "하루 약 N분". */
    val dailyMinutes: Int get() = (weeklyMinutes / DAYS_IN_WEEK.toDouble()).roundToInt()
    val totalMinutes: Int get() = weeklyMinutes * weeks

    private companion object {
        const val DAYS_IN_WEEK = 7
    }
}
