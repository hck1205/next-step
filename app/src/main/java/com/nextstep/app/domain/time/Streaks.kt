package com.nextstep.app.domain.time

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 이어서 한 날 세기(연속). 날짜 한 벌로만 셉니다 — 공부 연속 · 습관 · 게임 요소의 연속 · 성장 앨범이 같은 규칙을 씁니다.
 * [restDays] 만큼은 쉬어도 이어진 것으로 보고(쉰 날은 세지 않음), 그보다 길게 쉬면 끊깁니다.
 */
object Streaks {

    /** 오늘까지 이어진 날 수. 오늘 아직이면 어제부터 셉니다(하루가 끝나기 전에 끊긴 것처럼 보이지 않게). */
    fun current(days: Set<LocalDate>, today: LocalDate, restDays: Int = 0): Int {
        var day = today
        var gap = 0
        while (day !in days) {
            gap++
            if (gap > restDays + 1) return 0
            day = day.minusDays(1)
        }
        var n = 0
        gap = 0
        while (gap <= restDays) {
            if (day in days) { n++; gap = 0 } else gap++
            day = day.minusDays(1)
        }
        return n
    }

    /** 가장 길게 이어진 날 수. */
    fun longest(days: Set<LocalDate>, restDays: Int = 0): Int {
        var best = 0
        var run = 0
        var prev: LocalDate? = null
        days.sorted().forEach { d ->
            run = if (prev != null && ChronoUnit.DAYS.between(prev, d) - 1 <= restDays) run + 1 else 1
            prev = d
            best = maxOf(best, run)
        }
        return best
    }
}
