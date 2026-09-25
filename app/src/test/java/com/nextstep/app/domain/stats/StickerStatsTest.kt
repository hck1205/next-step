package com.nextstep.app.domain.stats

import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class StickerStatsTest {
    private val today = LocalDate.of(2029, 10, 10)

    @Test
    fun boardCoversFourWeeksEndingTodayWithStarsAndFlowers() {
        val sessions = listOf(Fixtures.session("math", today, LocalTime.of(9, 0), 10), Fixtures.session("math", today.minusDays(3), LocalTime.of(9, 0), 10), Fixtures.session("math", today.minusDays(40), LocalTime.of(9, 0), 10))
        val activities = listOf(Fixtures.activity("그림 그리기", date = today.minusDays(3)), Fixtures.activity("나들이", date = today.minusDays(5)), Fixtures.activity("지움", date = today).copy(deleted = true))
        val tasks = listOf(Fixtures.task("a", today, done = true), Fixtures.task("b", today.minusDays(6), done = true), Fixtures.task("c", today.minusDays(7), done = true), Fixtures.task("d", today))
        val b = StickerStats.board(sessions, tasks, activities, today)
        assertEquals(StickerStats.BOARD_DAYS, b.days.size); assertEquals(today, b.days.last().date); assertEquals(today.minusDays(27), b.days.first().date)
        assertEquals(3, b.stickers) // 오늘(별), 3일 전(별+꽃), 5일 전(꽃)
        assertTrue(b.days.last().studied); assertFalse(b.days.last().didActivity)
        val threeAgo = b.days.first { it.date == today.minusDays(3) }
        assertTrue(threeAgo.studied && threeAgo.didActivity)
        assertEquals(2, b.doneThisWeek)
    }
}
