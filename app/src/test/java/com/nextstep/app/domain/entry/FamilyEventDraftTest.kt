package com.nextstep.app.domain.entry

import com.nextstep.app.domain.familycalendar.FamilyEventKind
import com.nextstep.app.domain.familycalendar.FamilyHeadsUp
import com.nextstep.app.domain.familycalendar.FamilyRepeat
import com.nextstep.app.testing.Fixtures
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyEventDraftTest {
    private val day = LocalDate.of(2029, 3, 10)

    @Test
    fun newDraftDefaultsAndSaveRules() {
        val d = FamilyEventDraft.of(null, day)
        assertFalse(d.canSave); assertEquals(FamilyEventKind.FAMILY, d.kind); assertEquals(FamilyHeadsUp.DAY_BEFORE, d.headsUp); assertTrue(d.memberIds.isEmpty())
        val e = d.copy(
            title = "  치과 ", start = LocalTime.of(16, 0), end = LocalTime.of(15, 0), endDate = day.minusDays(2),
            memberIds = listOf("kid", "kid"), keeperId = "mom", bring = "보험증, 칫솔\n 칫솔 ,", repeatUntil = day.plusDays(9), location = " 역 앞 ",
        ).toEntity()
        assertEquals("치과", e.title); assertEquals(day.toEpochDay(), e.endDate) // 끝 날이 이르면 시작 날로
        assertEquals(16 * 60, e.startMinute); assertEquals(17 * 60, e.endMinute) // 끝 시각이 이르면 한 시간짜리
        assertEquals("kid", e.memberIds); assertEquals("mom", e.keeperId); assertEquals(listOf("보험증", "칫솔"), e.bringList)
        assertNull(e.repeatUntil) // 반복하지 않으면 끝 날 없음
        assertEquals("역 앞", e.location)
    }

    @Test
    fun editingKeepsIdAndRoundTrips() {
        val existing = Fixtures.familyEvent("수영", day, repeat = "WEEKLY", until = day.plusMonths(1), members = "kid", minutes = 17 * 60 to 18 * 60).copy(bring = "수경\n수모", createdById = "mom")
        val d = FamilyEventDraft.of(existing, day.plusDays(3))
        assertEquals(day, d.startDate); assertEquals(LocalTime.of(17, 0), d.start); assertEquals(FamilyRepeat.WEEKLY, d.repeat); assertEquals("수경, 수모", d.bring)
        val saved = d.copy(title = "수영 강습").toEntity(existing)
        assertEquals(existing.id, saved.id); assertEquals("mom", saved.createdById); assertEquals("수영 강습", saved.title)
        assertEquals(existing.repeatUntil, saved.repeatUntil); assertEquals(existing.bring, saved.bring); assertEquals(existing.endMinute, saved.endMinute)
    }

    @Test
    fun kindDefaultsMembersAndDates() {
        val d = FamilyEventDraft.of(null, day)
        val birthday = d.withKind(FamilyEventKind.CELEBRATION, isNew = true)
        assertTrue(birthday.allDay); assertEquals(FamilyRepeat.YEARLY, birthday.repeat); assertEquals(FamilyHeadsUp.WEEK, birthday.headsUp)
        assertFalse(d.withKind(FamilyEventKind.CELEBRATION, isNew = false).allDay)
        assertTrue(d.withKind(FamilyEventKind.OUTING, isNew = true).allDay)
        assertEquals(listOf("kid"), d.toggleMember("kid").memberIds); assertTrue(d.toggleMember("kid").toggleMember("kid").memberIds.isEmpty())
        val moved = d.copy(endDate = day.plusDays(1)).withStartDate(day.plusDays(5))
        assertEquals(day.plusDays(5), moved.endDate)
        assertEquals(day.plusDays(9), d.copy(endDate = day.plusDays(9)).withStartDate(day.plusDays(2)).endDate)
        assertEquals(0, d.copy(title = "여행", allDay = true).toEntity().startMinute)
        assertEquals(listOf("a", "b"), FamilyEventDraft.items(" a,,b\na "))
    }
}
