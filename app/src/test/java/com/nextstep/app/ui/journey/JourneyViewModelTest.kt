package com.nextstep.app.ui.journey

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.growth.GrowthStage
import com.nextstep.app.domain.journey.JourneyPhase
import com.nextstep.app.domain.journey.MilestoneCatalog
import com.nextstep.app.domain.journey.MilestoneCategory
import com.nextstep.app.fake.FakeFamilyDataStreams
import com.nextstep.app.fake.FakeGoalRepository
import com.nextstep.app.fake.FakeJourneyRepository
import com.nextstep.app.fake.FakeMemberRepository
import com.nextstep.app.fake.FakeTaskRepository
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
    private val goals = FakeGoalRepository()
    private val tasks = FakeTaskRepository()
    private val today = LocalDate.of(2026, 9, 22)

    private fun vm() = JourneyViewModel(streams, journey, members, goals, tasks, today = { today })

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
        // 저장된 마감일(2026-09-01)이 카탈로그 계산값과 같아야 같은 구간(age-0)에 남습니다
        streams.journeyItems.value = listOf(Fixtures.journeyItem("daycare-waitlist", MilestoneStatus.DONE, due = LocalDate.of(2026, 9, 1)))
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertTrue(s.hasBirthDate); assertEquals(GrowthStage.NEWBORN, s.stage); assertEquals("만 0세 2개월", s.ageLabel)
        assertEquals(MilestoneCatalog.templates.size, s.items.size)
        assertEquals("age-0", s.currentPeriodKey); assertEquals(0, s.pastSectionCount)
        val sections = s.periodSections
        assertTrue(sections.first().isCurrent); assertTrue(sections.none { it.isPast })
        assertTrue(sections.first().milestones.any { it.templateId == "vaccine-2m" })
        assertTrue(sections.none { sec -> sec.milestones.any { it.templateId == "daycare-waitlist" } })
        assertEquals(sections.map { it.period.start }, sections.map { it.period.start }.sorted())
        assertTrue(s.overdueCount >= 1); assertTrue(s.nowCount >= 1); assertTrue(s.completion > 0f)
        vm.onEvent(JourneyEvent.ShowCompleted(true)); s = settle(vm.state)
        assertTrue(s.periodSections.first().milestones.any { it.templateId == "daycare-waitlist" })
        assertEquals(listOf(JourneyPhase.OVERDUE, JourneyPhase.NOW, JourneyPhase.UPCOMING, JourneyPhase.DONE), s.phaseSections.map { it.first })
        vm.onEvent(JourneyEvent.SetFilter(MilestoneCategory.HEALTH)); s = settle(vm.state)
        assertTrue(s.filtered.isNotEmpty()); assertTrue(s.filtered.all { it.category == MilestoneCategory.HEALTH })
        job.cancel()
    }

    @Test
    fun goalStepsAppearInTheirPeriodAndPastSectionsToggle() = runTest {
        val born = LocalDate.of(2020, 5, 15) // 2026-09-22 → 만 6세 전반(age-72) … 실제 구간은 달력이 정함
        streams.members.value = listOf(Fixtures.member(Role.STUDENT, "아이", id = "kid", birthDate = born))
        streams.goals.value = listOf(Fixtures.goal("영어", id = "g"))
        val current = com.nextstep.app.domain.journey.PeriodCalendar.current(born, today)!!
        val past = com.nextstep.app.domain.journey.PeriodCalendar.periods(born).first()
        streams.goalSteps.value = listOf(
            Fixtures.step("g", current.key, "지금 단계", id = "now"),
            Fixtures.step("g", past.key, "지난 단계", id = "old"),
            Fixtures.step("g", current.key, "끝난 단계", id = "done", status = MilestoneStatus.DONE),
            Fixtures.step("zzz", current.key, "삭제된 목표의 단계", id = "orphan"),
        )
        streams.activities.value = listOf(
            Fixtures.activity("과학관", date = current.start.plusDays(3)),
            Fixtures.activity("옛날 소풍", date = past.start),
            Fixtures.activity("지워짐", date = current.start).copy(deleted = true),
        )
        val vm = vm(); val job = subscribe(vm.state)
        var s = settle(vm.state)
        assertEquals(current.key, s.currentPeriodKey); assertTrue(s.pastSectionCount > 0)
        assertEquals(listOf("과학관"), s.periodSections.first { it.isCurrent }.activities.map { it.title })
        val cur = s.periodSections.first { it.isCurrent }
        assertEquals(listOf("지금 단계"), cur.steps.map { it.step.title }); assertEquals("영어", cur.steps.single().goalTitle)
        assertTrue(s.periodSections.none { it.isPast })
        vm.onEvent(JourneyEvent.ShowPast(true)); vm.onEvent(JourneyEvent.ShowCompleted(true)); s = settle(vm.state)
        assertEquals(listOf("지난 단계"), s.periodSections.first { it.period.key == past.key }.steps.map { it.step.title })
        assertEquals(listOf("옛날 소풍"), s.periodSections.first { it.period.key == past.key }.activities.map { it.title })
        assertEquals(setOf("지금 단계", "끝난 단계"), s.periodSections.first { it.isCurrent }.steps.map { it.step.title }.toSet())
        vm.onEvent(JourneyEvent.SetStepStatus(streams.goalSteps.value[0], MilestoneStatus.DONE))
        vm.onEvent(JourneyEvent.SendStepToTasks(streams.goalSteps.value[0], "PARENT"))
        vm.onEvent(JourneyEvent.SendStepToTasks(streams.goalSteps.value[0].copy(taskId = "t"), "PARENT"))
        settle(vm.state)
        assertEquals(listOf("stepStatus:now:DONE", "stepTask:now:set"), goals.calls)
        assertEquals(current.end.toEpochDay(), tasks.saved.single().dueDate); assertEquals("영어", tasks.saved.single().note)
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
