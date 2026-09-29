package com.nextstep.app.domain.period

/** 한 기간의 숫자(아이 자신의 기록만). [scores] 는 과목 이름 → 평균 점수(0~100). */
data class PeriodStats(
    val studyMinutes: Int = 0,
    val studyDays: Int = 0,
    val tasksDue: Int = 0,
    val tasksDone: Int = 0,
    val scores: Map<String, Int> = emptyMap(),
    val activities: Int = 0,
    val goalsDone: Int = 0,
    val cheers: Int = 0,
) {
    val doneRate: Int? get() = if (tasksDue == 0) null else tasksDone * PERCENT / tasksDue

    private companion object {
        const val PERCENT = 100
    }
}
