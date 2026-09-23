package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.dao.FakeActivityDao
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

class RoomActivityRepositoryTest {
    private val dao = FakeActivityDao()
    private val sync = RecordingSyncManager()
    private val repo = RoomActivityRepository(dao, FakeFamilyScope(Role.PARENT), sync, FakeTimeSource(10L))

    @Test
    fun saveNormalizesFieldsAndStampsAuthorOnce() = runTest {
        repo.save(Fixtures.activity("  ", place = "x"))
        assertTrue(dao.all.isEmpty())
        val start = LocalDate.of(2029, 3, 5)
        repo.save(Fixtures.activity(" 로봇반 ", ActivityType.CLUB, start, end = start.minusDays(1), place = " 학교 ", note = " 재밌었다 ", rating = 9).copy(familyId = ""))
        val row = dao.all.single()
        assertEquals("로봇반", row.title); assertEquals("학교", row.place); assertEquals("재밌었다", row.note); assertEquals(5, row.rating)
        assertNull(row.endDate) // 시작일보다 앞선 종료일은 버림
        assertEquals(Fixtures.FAMILY, row.familyId); assertEquals("PARENT", row.createdByRole); assertEquals(10L, row.updatedAt); assertTrue(row.dirty)
        RoomActivityRepository(dao, FakeFamilyScope(Role.STUDENT), sync, FakeTimeSource(20L)).save(row.copy(rating = -1, endDate = start.plusDays(3).toEpochDay()))
        assertEquals("PARENT", dao.getById(row.id)!!.createdByRole); assertEquals(0, dao.getById(row.id)!!.rating); assertEquals(start.plusDays(3).toEpochDay(), dao.getById(row.id)!!.endDate)
        assertEquals(2, sync.pushRequests)
    }

    @Test
    fun deleteIsSoftAndStreamHidesIt() = runTest {
        dao.seed(Fixtures.activity("소풍", id = "a1"), Fixtures.activity("여행", id = "a2"))
        repo.delete("a1"); repo.delete("nope")
        assertTrue(dao.getById("a1")!!.deleted)
        assertEquals(listOf("a2"), repo.activities.first().map { it.id })
        assertEquals(1, sync.pushRequests)
    }
}
