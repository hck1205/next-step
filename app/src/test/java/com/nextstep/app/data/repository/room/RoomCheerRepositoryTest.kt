package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.cheer.CheerKind
import com.nextstep.app.fake.dao.FakeCheerDao
import com.nextstep.app.testing.FakeFamilyScope
import com.nextstep.app.testing.FakeTimeSource
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.testing.RecordingSyncManager
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomCheerRepositoryTest {
    private val dao = FakeCheerDao()
    private val sync = RecordingSyncManager()
    private val repo = RoomCheerRepository(dao, FakeFamilyScope(Role.PARENT), sync, FakeTimeSource(7L))
    private val task = Fixtures.task("분수 20문제", LocalDate.of(2029, 3, 7), done = true)

    @Test
    fun oneCheerPerTaskAndSenderThatChangesOrIsTakenBack() = runTest {
        repo.set(task, CheerKind.CLAP, "엄마")
        val c = dao.all.single()
        assertEquals("CLAP", c.kind); assertEquals("me", c.fromId); assertEquals("PARENT", c.fromRole); assertEquals("엄마", c.fromName); assertEquals("분수 20문제", c.taskTitle)
        repo.markSeen(listOf(c.id))
        assertEquals(7L, dao.getById(c.id)!!.seenAt)
        repo.set(task, CheerKind.STAR, "엄마") // 바꾸면 아이에게 다시 보임
        assertEquals("STAR", dao.getById(c.id)!!.kind); assertNull(dao.getById(c.id)!!.seenAt); assertEquals(1, dao.all.size)
        repo.set(task, null, "엄마")
        assertTrue(dao.getById(c.id)!!.deleted); assertTrue(repo.cheers.first().isEmpty())
        assertTrue(sync.pushRequests >= 4)
    }
}
