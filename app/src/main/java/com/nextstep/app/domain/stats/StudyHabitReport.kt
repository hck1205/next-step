package com.nextstep.app.domain.stats

import java.time.DayOfWeek

/**
 * 최근 [days]일의 공부 습관. 비교 대상은 또래가 아니라 지난 기록뿐입니다.
 * [lines] 는 화면 맨 위에 그대로 쓰는 문장(많아야 4줄)입니다.
 */
data class StudyHabitReport(
    val days: Int,
    val totalMinutes: Int,
    val activeDays: Int,
    val sessionCount: Int,
    val averageSessionMinutes: Int,
    val longestSessionMinutes: Int,
    val byPart: Map<DayPart, Int>,
    val byWeekday: Map<DayOfWeek, Int>,
    val bestPart: DayPart?,
    val bestWeekday: DayOfWeek?,
    val currentStreak: Int,
    val longestStreak: Int,
    val thisWeekMinutes: Int,
    val lastWeekMinutes: Int,
    /** 타이머로 잰 공부의 비율(0~100). 직접 적은 기록보다 믿을 만한 정도. */
    val timerPercent: Int,
    val lines: List<String>,
) {
    val isEmpty: Boolean get() = sessionCount == 0
}
