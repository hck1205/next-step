package com.nextstep.app.domain.stats

import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionDatesTest {
    private val day = LocalDate.of(2029, 5, 10)

    @Test
    fun studyDaysCountEachDayOnceAndSkipDeleted() {
        val sessions = listOf(
            Fixtures.session("math", day, LocalTime.of(9, 0), 30),
            Fixtures.session("eng", day, LocalTime.of(20, 0), 30),
            Fixtures.session("math", day.minusDays(1), LocalTime.of(9, 0), 30),
            Fixtures.session("math", day.minusDays(2), LocalTime.of(9, 0), 30).copy(deleted = true),
        )
        assertEquals(day, sessions.first().day())
        assertEquals(setOf(day, day.minusDays(1)), sessions.studyDays())
    }
}
