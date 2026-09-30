package com.nextstep.app.domain.time

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class StreaksTest {
    private val today = LocalDate.of(2029, 5, 10)
    private fun back(vararg n: Long) = n.map { today.minusDays(it) }.toSet()

    @Test
    fun currentCountsUpToTodayOrYesterday() {
        assertEquals(3, Streaks.current(back(0, 1, 2, 4), today))
        assertEquals(2, Streaks.current(back(1, 2), today)) // 오늘 아직이면 어제부터
        assertEquals(0, Streaks.current(back(2, 3), today))
        assertEquals(0, Streaks.current(emptySet(), today))
    }

    @Test
    fun restDaysKeepTheStreakWithoutCountingThem() {
        assertEquals(3, Streaks.current(back(0, 2, 3), today, restDays = 1))
        assertEquals(1, Streaks.current(back(0, 3), today, restDays = 1))
        assertEquals(2, Streaks.current(back(2, 4), today, restDays = 1))
    }

    @Test
    fun longestFindsTheLongestRun() {
        assertEquals(3, Streaks.longest(back(0, 5, 6, 7, 9)))
        assertEquals(4, Streaks.longest(back(0, 5, 6, 7, 9), restDays = 1))
        assertEquals(0, Streaks.longest(emptySet()))
    }
}
