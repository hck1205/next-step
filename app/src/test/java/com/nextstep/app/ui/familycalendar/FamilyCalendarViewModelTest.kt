package com.nextstep.app.ui.familycalendar

import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.entry.FamilyEventDraft
import com.nextstep.app.domain.familycalendar.FamilyEventKind
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeFamilyEventRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FamilyCalendarViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val repo = FakeFamilyEventRepository(streams)
    private val today = LocalDate.of(2029, 3, 14)

    private fun vm() = FamilyCalendarViewModel(streams, repo, today = { today })

    @Test
    fun monthDaysAndSelectedDayShowFamilyAndChildSchedule() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "지우", id = "kid"), Fixtures.member(Role.PARENT, "엄마", id = "mom"), Fixtures.member(Role.MENTOR, "쌤", id = "t"))
        streams.familyEvents.value = listOf(
            Fixtures.familyEvent("수영", today, repeat = "WEEKLY", members = "kid", kind = "LESSON"),
            Fixtures.familyEvent("엄마 출장", today.plusDays(1), end = today.plusDays(2), members = "mom", kind = "WORK"),
            Fixtures.familyEvent("외식", today),
        )
        streams.events.value = listOf(Fixtures.event("학원", today, LocalTime.of(17, 0), LocalTime.of(18, 0), EventType.ACADEMY))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertEquals(YearMonth.of(2029, 3), s.month); assertEquals(listOf("지우", "엄마"), s.members.map { it.name })
        assertEquals(listOf("수영", "외식"), s.dayEvents.map { it.event.title })
        assertEquals(listOf(today, today.plusDays(7), today.plusDays(14)), s.days.filterValues { d -> d.any { it.event.title == "수영" } }.keys.sorted())
        assertEquals(listOf("엄마 출장"), s.days.getValue(today.plusDays(2)).map { it.event.title })
        assertTrue(today in s.studyDays); assertEquals(listOf("학원"), s.dayStudy.map { it.event.title }); assertEquals("지우", s.studentName)
        // 엄마만: 엄마 일정 + 가족 모두의 일정, 아이 공부 일정은 빠짐
        vm.onEvent(FamilyCalendarEvent.Filter("mom"))
        val mom = settle(vm.state)
        assertEquals(listOf("외식"), mom.dayEvents.map { it.event.title }); assertTrue(mom.studyDays.isEmpty()); assertTrue(mom.dayStudy.isEmpty())
        assertTrue(mom.days.containsKey(today.plusDays(1)))
        // 떠난 사람으로 거르면 모두로
        vm.onEvent(FamilyCalendarEvent.Filter("gone"))
        assertNull(settle(vm.state).filter)
        job.cancel()
    }

    @Test
    fun navigationSaveEditAndDelete() = runTest {
        val vm = vm(); val job = subscribe(vm.state); settle(vm.state)
        vm.onEvent(FamilyCalendarEvent.NextMonth); assertEquals(YearMonth.of(2029, 4), settle(vm.state).month)
        vm.onEvent(FamilyCalendarEvent.Select(LocalDate.of(2029, 5, 2)))
        val picked = settle(vm.state)
        assertEquals(LocalDate.of(2029, 5, 2), picked.selected); assertEquals(YearMonth.of(2029, 5), picked.month)
        vm.onEvent(FamilyCalendarEvent.Today); assertEquals(today, settle(vm.state).selected)
        val draft = FamilyEventDraft.of(null, today)
        vm.onEvent(FamilyCalendarEvent.Save(null, draft)) // 제목이 없으면 저장하지 않음
        vm.onEvent(FamilyCalendarEvent.Save(null, draft.copy(title = "할머니 생신").withKind(FamilyEventKind.CELEBRATION, isNew = true)))
        val saved = settle(vm.state).dayEvents.single().event
        assertEquals("할머니 생신", saved.title); assertEquals("YEARLY", saved.repeat); assertEquals(1, repo.saved.size)
        vm.onEvent(FamilyCalendarEvent.Save(saved, FamilyEventDraft.of(saved, today).copy(title = "할머니 생신 잔치")))
        assertEquals(listOf("할머니 생신 잔치"), settle(vm.state).dayEvents.map { it.event.title })
        vm.onEvent(FamilyCalendarEvent.Delete(saved.id))
        assertTrue(settle(vm.state).dayEvents.isEmpty())
        job.cancel()
    }
}
