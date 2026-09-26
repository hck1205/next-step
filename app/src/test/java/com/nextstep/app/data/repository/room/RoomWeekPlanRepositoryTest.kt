package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.dao.FakeWeekPlanDao
import com.nextstep.app.testing.FakeFamilyScope
import com.nextstep.app.testing.FakeTimeSource
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.testing.RecordingSyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class RoomWeekPlanRepositoryTest {
    private val dao = FakeWeekPlanDao()
    private val sync = RecordingSyncManager()
    private val repo = RoomWeekPlanRepository(dao, FakeFamilyScope(Role.STUDENT), sync, FakeTimeSource(7L))
    private val wednesday = LocalDate.of(2029, 3, 7)
    private val monday = LocalDate.of(2029, 3, 5)

    @Test
    fun planIsOnePerWeekTrimmedAndCappedAtThreeGoals() = runTest {
        repo.savePlan(wednesday, listOf(" ", ""), 0)
        assertTrue(dao.all.isEmpty())
        repo.savePlan(wednesday, listOf(" 영어 책 ", "줄넘기", "", "피아노", "코딩"), 99_999)
        val row = dao.all.single()
        assertEquals(monday.toEpochDay(), row.weekStart); assertEquals(listOf("영어 책", "줄넘기", "피아노"), row.goalList)
        assertEquals(3000, row.plannedMinutes); assertEquals("STUDENT", row.authorRole); assertEquals(Fixtures.FAMILY, row.familyId)
        repo.toggleGoal(row.id, 1); repo.toggleGoal(row.id, 5)
        assertTrue(dao.getById(row.id)!!.isDone(1)); assertEquals(1, dao.getById(row.id)!!.doneCount)
        repo.approve(row.id)
        val approvedAt = dao.getById(row.id)!!.approvedAt
        assertNotNull(approvedAt)
        repo.savePlan(monday, listOf("영어 책", "줄넘기", "피아노"), 3000) // 같은 내용이면 표시·확인 유지
        assertEquals(1, dao.all.size); assertTrue(dao.getById(row.id)!!.isDone(1)); assertEquals(approvedAt, dao.getById(row.id)!!.approvedAt)
        repo.savePlan(monday, listOf("영어 책"), 120) // 바뀌면 다시 확인
        assertNull(dao.getById(row.id)!!.approvedAt); assertFalse(dao.getById(row.id)!!.isDone(1))
        assertTrue(sync.pushRequests >= 4)
    }

    @Test
    fun reflectionNeedsAMoodAndWorksWithoutAPlan() = runTest {
        repo.reflect(wednesday, 0, "a", "", "")
        assertTrue(dao.all.isEmpty())
        repo.reflect(wednesday, 3, " 매일 읽었다 ", "", " 아침에 하기 ")
        val row = repo.plans.first().single()
        assertEquals(3, row.mood); assertEquals("매일 읽었다", row.good); assertEquals("아침에 하기", row.change)
        assertEquals("STUDENT", row.reflectedByRole); assertTrue(row.isReflected); assertFalse(row.hasPlan)
    }
}
