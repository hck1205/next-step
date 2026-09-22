package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.dao.FakeGoalDao
import com.nextstep.app.fake.dao.FakeGoalStepDao
import com.nextstep.app.testing.FakeFamilyScope
import com.nextstep.app.testing.FakeTimeSource
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.testing.RecordingSyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomGoalRepositoryTest {
    private val goalDao = FakeGoalDao()
    private val stepDao = FakeGoalStepDao()
    private val sync = RecordingSyncManager()
    private val time = FakeTimeSource(500L)
    private val repo = RoomGoalRepository(goalDao, stepDao, FakeFamilyScope(Role.PARENT), sync, time)

    @Test
    fun addFillsFamilyAuthorAndLinksSteps() = runTest {
        val goal = Fixtures.goal("영어", trackId = "english-early").copy(familyId = "", title = " 영어 ")
        repo.add(goal, listOf(Fixtures.step("other", "g1s1", "a").copy(familyId = ""), Fixtures.step("other", "g1s2", "b").copy(familyId = "", id = "s2")))
        val saved = goalDao.all.single()
        assertEquals(Fixtures.FAMILY, saved.familyId); assertEquals("영어", saved.title); assertEquals("PARENT", saved.createdByRole); assertTrue(saved.dirty)
        assertEquals(2, stepDao.all.size)
        assertTrue(stepDao.all.all { it.goalId == goal.id && it.familyId == Fixtures.FAMILY && it.dirty })
        assertEquals(1, sync.pushRequests)
        repo.add(goal.copy(id = "blank", title = "  "), emptyList())
        assertEquals(1, goalDao.all.size)
    }

    @Test
    fun stepStatusTaskAndGoalStatusUpdateRows() = runTest {
        stepDao.seed(Fixtures.step("g", "g1s1", "a", id = "s1"))
        goalDao.seed(Fixtures.goal("수학", id = "g"))
        repo.setStepStatus("s1", MilestoneStatus.DONE)
        assertEquals(MilestoneStatus.DONE, stepDao.getById("s1")!!.status); assertEquals(500L, stepDao.getById("s1")!!.doneAt)
        repo.setStepStatus("s1", MilestoneStatus.UPCOMING); assertNull(stepDao.getById("s1")!!.doneAt)
        repo.setStepTask("s1", "t1"); assertEquals("t1", stepDao.getById("s1")!!.taskId)
        repo.setGoalStatus("g", GoalStatus.DONE); assertEquals(GoalStatus.DONE, goalDao.getById("g")!!.status)
        repo.setStepStatus("nope", MilestoneStatus.DONE); repo.setGoalStatus("nope", GoalStatus.DONE)
        assertEquals(4, sync.pushRequests)
    }

    @Test
    fun addStepValidatesAndDeleteSoftDeletesGoalWithSteps() = runTest {
        goalDao.seed(Fixtures.goal("수학", id = "g"))
        repo.addStep(Fixtures.step("g", "g1s1", "  ").copy(familyId = ""))
        assertTrue(stepDao.all.isEmpty())
        repo.addStep(Fixtures.step("g", "g1s1", " 첫 단계 ", id = "s1").copy(familyId = ""))
        repo.addStep(Fixtures.step("g", "g1s2", "둘째", id = "s2"))
        repo.addStep(Fixtures.step("other", "g1s1", "남", id = "s3"))
        assertEquals("첫 단계", stepDao.getById("s1")!!.title); assertEquals(Fixtures.FAMILY, stepDao.getById("s1")!!.familyId)
        repo.delete("g")
        assertTrue(goalDao.getById("g")!!.deleted)
        assertTrue(stepDao.getById("s1")!!.deleted); assertTrue(stepDao.getById("s2")!!.deleted); assertTrue(!stepDao.getById("s3")!!.deleted)
        assertTrue(repo.goals.first().isEmpty()); assertEquals(listOf("s3"), repo.steps.first().map { it.id })
        repo.delete("nope")
    }
}
