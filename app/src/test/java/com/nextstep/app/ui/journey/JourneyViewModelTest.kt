package com.nextstep.app.ui.journey

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyPhase
import com.nextstep.app.domain.journey.MilestoneCatalog
import com.nextstep.app.domain.journey.MilestoneCategory
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeJourneyRepository
import com.nextstep.app.fake.FakeMemberRepository
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.ui.ViewModelTestBase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class JourneyViewModelTest : ViewModelTestBase() {
    private val streams = FakeFamilyDataStreams(role = Role.PARENT)
    private val journey = FakeJourneyRepository()
    private val members = FakeMemberRepository()
    private val today = LocalDate.of(2026, 9, 22)

    private fun vm() = JourneyViewModel(streams, journey, members, today = { today })

    @Test
    fun withoutBirthDateStateAsksForItAndSetBirthDateTargetsStudent() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.PARENT, "엄마", id = "me"), Fixtures.member(Role.STUDENT, "아이", id = "kid"))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        assertTrue(s.loaded); assertFalse(s.hasBirthDate); assertNull(s.stage); assertTrue(s.items.isEmpty()); assertEquals("kid", s.studentMemberId)
        vm.onEvent(JourneyEvent.SetBirthDate(LocalDate.of(2026, 7, 1)))
        settle(vm.state)
        assertEquals(listOf("birth:kid:2026-07-01"), members.calls)
        job.cancel()
    }

    @Test
    fun birthDateBuildsTimelineSectionsAndFilters() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아이", id = "kid", birthDate = LocalDate.of(2026, 7, 1)))
        streams.journeyItems.value = listOf(Fixtures.journeyItem("daycare-waitlist", MilestoneStatus.DONE))
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertTrue(s.hasBirthDate); assertEquals(GrowthStage.NEWBORN, s.stage); assertEquals("만 0세 2개월", s.ageLabel)
        assertEquals(MilestoneCatalog.templates.size, s.items.size)
        assertEquals(listOf(JourneyPhase.OVERDUE, JourneyPhase.NOW, JourneyPhase.UPCOMING), s.sections.map { it.first })
        assertTrue(s.overdueCount >= 1); assertTrue(s.nowCount >= 1); assertTrue(s.completion > 0f)
        vm.onEvent(JourneyEvent.ShowCompleted(true)); s = settle(vm.state)
        assertTrue(s.sections.any { it.first == JourneyPhase.DONE })
        vm.onEvent(JourneyEvent.SetFilter(MilestoneCategory.HEALTH)); s = settle(vm.state)
        assertTrue(s.filtered.isNotEmpty()); assertTrue(s.filtered.all { it.category == MilestoneCategory.HEALTH })
        job.cancel()
    }

    @Test
    fun eventsRouteTemplateItemsByTemplateIdAndCustomItemsByEntityId() = runTest {
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아이", id = "kid", birthDate = LocalDate.of(2026, 7, 1)))
        streams.journeyItems.value = listOf(Fixtures.journeyItem(null, title = "설명회", due = today.plusMonths(2), id = "c1"))
        val vm = vm(); val job = subscribe(vm.state)
        val s = settle(vm.state)
        val template = s.items.first { it.templateId == "vaccine-2m" }
        val custom = s.items.first { it.isCustom }
        vm.onEvent(JourneyEvent.SetStatus(template, MilestoneStatus.DONE))
        vm.onEvent(JourneyEvent.SetNote(template, "완료"))
        vm.onEvent(JourneyEvent.SetDueDate(template, today))
        vm.onEvent(JourneyEvent.SetStatus(custom, MilestoneStatus.SKIPPED))
        vm.onEvent(JourneyEvent.SetNote(custom, "n")); vm.onEvent(JourneyEvent.SetDueDate(custom, today))
        vm.onEvent(JourneyEvent.DeleteCustom(custom)); vm.onEvent(JourneyEvent.DeleteCustom(template))
        vm.onEvent(JourneyEvent.AddCustom("영어유치원", "d", MilestoneCategory.LANGUAGE, today, 3))
        settle(vm.state)
        assertEquals(
            listOf("tStatus:vaccine-2m:DONE:2026-09-01", "tNote:vaccine-2m:완료", "tDue:vaccine-2m:2026-09-22", "status:c1:SKIPPED", "note:c1:n", "due:c1:2026-09-22", "delete:c1", "add:영어유치원:LANGUAGE:2026-09-22:3"),
            journey.calls,
        )
        job.cancel()
    }
}
