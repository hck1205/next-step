package com.nextstep.app.domain.journey

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.testing.Fixtures
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class JourneyPlannerTest {
    private val born = LocalDate.of(2026, 7, 1)
    private val today = LocalDate.of(2026, 9, 22) // 생후 2개월 3주

    @Test
    fun withoutBirthDateOnlyCustomItemsAppear() {
        val custom = Fixtures.journeyItem(null, title = "설명회", due = today.plusDays(10))
        val items = JourneyPlanner.build(null, listOf(custom, Fixtures.journeyItem("daycare-waitlist")), today)
        assertEquals(listOf("설명회"), items.map { it.title })
        assertTrue(items.single().isCustom); assertNull(items.single().stage)
    }

    @Test
    fun catalogItemsGetDatesFromBirthDateAndPhasesFromToday() {
        val items = JourneyPlanner.build(born, emptyList(), today)
        assertEquals(MilestoneCatalog.templates.size, items.size)
        val daycare = items.first { it.templateId == "daycare-waitlist" }
        assertEquals(LocalDate.of(2026, 9, 1), daycare.dueDate)
        assertEquals(JourneyPhase.OVERDUE, daycare.phase(today))
        val checkup = items.first { it.templateId == "infant-checkup-1" }
        assertEquals(LocalDate.of(2026, 12, 1), checkup.dueDate); assertEquals(LocalDate.of(2026, 11, 1), checkup.startDate)
        assertEquals(JourneyPhase.UPCOMING, checkup.phase(today))
        val talk = items.first { it.templateId == "talk-daily" }
        assertEquals(JourneyPhase.NOW, talk.phase(today))
        assertTrue(items.first { it.templateId == "csat-register" }.dueDate.year > 2040)
    }

    @Test
    fun storedStateOverridesStatusNoteAndDueDate() {
        val stored = listOf(
            Fixtures.journeyItem("daycare-waitlist", MilestoneStatus.DONE, note = "9/1 신청"),
            Fixtures.journeyItem("vaccine-2m", MilestoneStatus.UPCOMING, due = LocalDate.of(2026, 10, 15)),
            Fixtures.journeyItem("birth-registration", MilestoneStatus.SKIPPED).copy(deleted = true),
        )
        val items = JourneyPlanner.build(born, stored, today)
        val daycare = items.first { it.templateId == "daycare-waitlist" }
        assertEquals(MilestoneStatus.DONE, daycare.status); assertEquals("9/1 신청", daycare.note); assertEquals("j-daycare-waitlist", daycare.entityId)
        assertEquals(JourneyPhase.DONE, daycare.phase(today))
        val vaccine = items.first { it.templateId == "vaccine-2m" }
        assertEquals(LocalDate.of(2026, 10, 15), vaccine.dueDate); assertEquals(JourneyPhase.NOW, vaccine.phase(today))
        // 삭제된 저장 행은 무시되어 카탈로그 기본값으로 돌아감
        assertEquals(MilestoneStatus.UPCOMING, items.first { it.templateId == "birth-registration" }.status)
    }

    @Test
    fun orderIsOverdueThenNowThenUpcomingThenDoneByDate() {
        val stored = listOf(Fixtures.journeyItem("child-benefit", MilestoneStatus.DONE), Fixtures.journeyItem(null, title = "직접", due = today.plusYears(30)))
        val items = JourneyPlanner.build(born, stored, today)
        val phases = items.map { it.phase(today) }
        val order = listOf(JourneyPhase.OVERDUE, JourneyPhase.NOW, JourneyPhase.UPCOMING, JourneyPhase.DONE, JourneyPhase.SKIPPED)
        assertEquals(phases, phases.sortedBy { order.indexOf(it) })
        val upcoming = items.filter { it.phase(today) == JourneyPhase.UPCOMING }
        assertEquals(upcoming.map { it.dueDate }, upcoming.map { it.dueDate }.sorted())
        assertEquals("직접", upcoming.last().title)
    }

    @Test
    fun actionableTakesOverdueAndNowOnlyAndCompletionCountsPassedItems() {
        val stored = listOf(Fixtures.journeyItem("daycare-waitlist", MilestoneStatus.DONE), Fixtures.journeyItem("birth-registration", MilestoneStatus.SKIPPED))
        val items = JourneyPlanner.build(born, stored, today)
        val actionable = JourneyPlanner.actionable(items, today, limit = 10)
        assertTrue(actionable.all { it.phase(today) == JourneyPhase.OVERDUE || it.phase(today) == JourneyPhase.NOW })
        assertTrue(actionable.none { it.templateId == "daycare-waitlist" })
        assertEquals(2, JourneyPlanner.actionable(items, today, limit = 2).size)
        // 지난 항목: 출생신고(건너뜀)·아동수당(8/1 지남)·어린이집(완료) + 지금 진행 중인 항목은 제외 → 완료 1 / 3
        val passed = items.filter { it.status != MilestoneStatus.UPCOMING || it.dueDate.isBefore(today) }
        assertEquals(1f / passed.size, JourneyPlanner.completion(items, today), 0.0001f)
        assertEquals(0f, JourneyPlanner.completion(emptyList(), today), 0f)
    }
}
