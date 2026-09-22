package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.fake.dao.FakeJourneyDao
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
import java.time.LocalDate

class RoomJourneyRepositoryTest {
    private val dao = FakeJourneyDao()
    private val sync = RecordingSyncManager()
    private val time = FakeTimeSource(1_000L)
    private val repo = RoomJourneyRepository(dao, FakeFamilyScope(), sync, time)
    private val due = LocalDate.of(2027, 3, 1)

    @Test
    fun templateStatusCreatesRowOnceThenUpdatesIt() = runTest {
        repo.setTemplateStatus("daycare-waitlist", MilestoneStatus.DONE, due)
        val row = dao.all.single()
        assertEquals("daycare-waitlist", row.templateId); assertEquals(Fixtures.FAMILY, row.familyId)
        assertEquals(MilestoneStatus.DONE, row.status); assertEquals(1_000L, row.doneAt); assertEquals(due.toEpochDay(), row.dueDate); assertTrue(row.dirty)
        repo.setTemplateStatus("daycare-waitlist", MilestoneStatus.UPCOMING, due)
        assertEquals(1, dao.all.size); assertNull(dao.all.single().doneAt)
        repo.setTemplateNote("daycare-waitlist", "  3월 신청  ", due)
        repo.setTemplateDueDate("daycare-waitlist", due.plusMonths(1))
        assertEquals("3월 신청", dao.all.single().note); assertEquals(due.plusMonths(1).toEpochDay(), dao.all.single().dueDate)
        assertEquals(4, sync.pushRequests)
    }

    @Test
    fun customItemsAreValidatedClampedAndSoftDeleted() = runTest {
        repo.addCustom("   ", "", "ADMIN", due, 1, 2)
        assertTrue(dao.all.isEmpty())
        repo.addCustom(" 설명회 ", " 메모 ", "LANGUAGE", due, -3, 9)
        val row = dao.all.single()
        assertEquals("설명회", row.title); assertEquals("메모", row.description); assertEquals("LANGUAGE", row.category)
        assertEquals(0, row.leadMonths); assertEquals(3, row.priority); assertNull(row.templateId)
        repo.setStatus(row.id, MilestoneStatus.DONE); assertEquals(MilestoneStatus.DONE, dao.getById(row.id)!!.status)
        repo.setNote(row.id, "n"); repo.setDueDate(row.id, due.plusDays(1))
        assertEquals("n", dao.getById(row.id)!!.note); assertEquals(due.plusDays(1).toEpochDay(), dao.getById(row.id)!!.dueDate)
        repo.delete(row.id)
        assertTrue(dao.getById(row.id)!!.deleted)
        assertTrue(repo.items.first().isEmpty())
        repo.setStatus("nope", MilestoneStatus.DONE); repo.delete("nope")
        assertEquals(1, dao.all.size)
    }
}
