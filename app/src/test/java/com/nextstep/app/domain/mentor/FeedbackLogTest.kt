package com.nextstep.app.domain.mentor

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class FeedbackLogTest {
    private fun note(text: String, date: LocalDate, hour: Int = 10) =
        Fixtures.note(text, Role.MENTOR, "쌤").copy(createdAt = DateUtils.toMillis(date, LocalTime.of(hour, 0)))

    @Test
    fun groupsByWeekNewestFirst() {
        val today = LocalDate.of(2029, 10, 10) // 수요일, 이번 주 월요일은 10월 8일
        val notes = listOf(
            note("화요일", LocalDate.of(2029, 10, 9)),
            note("오늘", today, 9),
            note("지난주", LocalDate.of(2029, 10, 2)),
            note("3주 전", LocalDate.of(2029, 9, 20)),
            note("지움", today).copy(deleted = true),
        )
        val weeks = FeedbackLog.weeks(notes, today)
        assertEquals(listOf("이번 주", "지난주", "3주 전"), weeks.map { it.label })
        assertEquals(listOf("오늘", "화요일"), weeks[0].notes.map { it.text })
    }
}
