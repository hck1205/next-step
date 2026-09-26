package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.dao.FakeProjectLogDao
import com.nextstep.app.testing.FakeFamilyScope
import com.nextstep.app.testing.FakeTimeSource
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.testing.RecordingSyncManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomProjectRepositoryTest {
    private val dao = FakeProjectLogDao()
    private val sync = RecordingSyncManager()
    private val repo = RoomProjectRepository(dao, FakeFamilyScope(Role.PARENT), sync, FakeTimeSource(7L))

    @Test
    fun logsAreValidatedClampedAndSoftDeleted() = runTest {
        repo.log("g", "p1", "노래", 0, 100L)
        repo.log("", "p1", "노래", 10, 100L)
        assertTrue(dao.all.isEmpty())
        repo.log("g", "p1", " 영어 노래 ", 999, 100L)
        val row = dao.all.single()
        assertEquals("영어 노래", row.item); assertEquals(240, row.minutes); assertEquals(Fixtures.FAMILY, row.familyId)
        assertEquals("PARENT", row.authorRole); assertEquals(7L, row.updatedAt); assertTrue(row.dirty)
        assertEquals(1, repo.logs.first().size)
        repo.delete(row.id)
        assertTrue(repo.logs.first().isEmpty())
        assertTrue(dao.getById(row.id)!!.deleted)
        assertTrue(sync.pushRequests >= 2)
    }

    @Test
    fun toggleLogsOnceAndUndoesOnSecondTap() = runTest {
        repo.toggle("g", "p1", "노래", 10, 100L)
        assertEquals(10, repo.logs.first().single().minutes)
        repo.toggle("g", "p1", "노래", 10, 101L)
        assertEquals(2, repo.logs.first().size)
        repo.toggle("g", "p1", "노래", 10, 100L)
        assertEquals(listOf(101L), repo.logs.first().map { it.date })
    }
}
