package com.nextstep.app.domain.task

import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskDatesTest {
    private val day = LocalDate.of(2029, 5, 10)
    private val zone = ZoneOffset.UTC

    @Test
    fun doneOnIsTheDayItWasFinished() {
        val done = Fixtures.task("분수", day.minusDays(3), done = true).copy(doneAt = day.atTime(LocalTime.of(23, 0)).toInstant(zone).toEpochMilli())
        assertEquals(day, done.doneOn(zone))
        assertEquals(day.plusDays(1), done.doneOn(ZoneOffset.ofHours(9)))
    }

    @Test
    fun notDoneOrNoTimeHasNoDay() {
        assertNull(Fixtures.task("분수", day).copy(doneAt = 1L).doneOn(zone))
        assertNull(Fixtures.task("분수", day, done = true).doneOn(zone))
    }
}
