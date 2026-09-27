package com.nextstep.app.domain.entry

import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventDraftTest {
    private val date = LocalDate.of(2026, 9, 1)
    private fun draft(start: LocalTime = LocalTime.of(16, 0), end: LocalTime = LocalTime.of(17, 30)) =
        EventDraft(" 수학 학원 ", "math", EventType.ACADEMY, date, start, end, true, " 역삼 ", " 교재 ")

    @Test
    fun newEventTrimsTextAndKeepsTimes() {
        val e = draft().toEntity()
        assertEquals("수학 학원", e.title); assertEquals("역삼", e.location); assertEquals("교재", e.memo)
        assertEquals(DateUtils.toMillis(date, LocalTime.of(16, 0)), e.startAt)
        assertEquals(DateUtils.toMillis(date, LocalTime.of(17, 30)), e.endAt)
        assertTrue(e.repeatWeekly); assertEquals(EventType.ACADEMY, e.type); assertEquals("", e.familyId)
    }

    @Test
    fun endNotAfterStartBecomesOneHour() {
        val e = draft(start = LocalTime.of(18, 0), end = LocalTime.of(18, 0)).toEntity()
        assertEquals(DateUtils.toMillis(date, LocalTime.of(19, 0)), e.endAt)
    }

    @Test
    fun editKeepsIdAndFamily() {
        val old = EventEntity(familyId = "fam", title = "옛 일정", startAt = 0, endAt = 1, repeatWeekly = true)
        val e = draft().copy(subjectId = null, repeatWeekly = false).toEntity(old)
        assertEquals(old.id, e.id); assertEquals("fam", e.familyId); assertEquals("수학 학원", e.title)
        assertNull(e.subjectId); assertFalse(e.repeatWeekly)
    }

    @Test
    fun formStartsFromExistingOrFourPm() {
        val blank = EventDraft.of(null, date)
        assertEquals(date, blank.date); assertEquals(LocalTime.of(16, 0), blank.start); assertEquals(LocalTime.of(17, 0), blank.end)
        assertEquals(EventType.CLASS, blank.type); assertEquals("", blank.title)
        val saved = draft().toEntity()
        val back = EventDraft.of(saved, date.plusDays(3))
        assertEquals(date, back.date); assertEquals(LocalTime.of(17, 30), back.end); assertEquals("수학 학원", back.title)
    }
}
