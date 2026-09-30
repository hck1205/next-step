package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.dao.FakeFamilyEventDao
import com.nextstep.app.testing.FakeFamilyScope
import com.nextstep.app.testing.FakeTimeSource
import com.nextstep.app.testing.Fixtures
import com.nextstep.app.testing.RecordingSyncManager
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomFamilyEventRepositoryTest {
    private val dao = FakeFamilyEventDao()
    private val sync = RecordingSyncManager()
    private val repo = RoomFamilyEventRepository(dao, FakeFamilyScope(Role.PARENT), sync, FakeTimeSource(7L))
    private val day = LocalDate.of(2029, 3, 10)

    @Test
    fun saveFillsFamilyAndAuthorOnceThenEditsAndSoftDeletes() = runTest {
        repo.save(Fixtures.familyEvent("  ", day))
        assertTrue(dao.all.isEmpty())
        repo.save(Fixtures.familyEvent(" 치과 ", day).copy(familyId = ""))
        val e = dao.all.single()
        assertEquals("치과", e.title); assertEquals(Fixtures.FAMILY, e.familyId); assertEquals("me", e.createdById); assertEquals("PARENT", e.createdByRole)
        assertEquals(7L, e.createdAt); assertTrue(e.dirty)
        repo.save(e.copy(title = "치과 검진", createdById = "me"))
        assertEquals("치과 검진", dao.getById(e.id)!!.title); assertEquals("me", dao.getById(e.id)!!.createdById)
        repo.delete(e.id)
        assertTrue(dao.getById(e.id)!!.deleted); assertTrue(repo.events.first().isEmpty())
        assertTrue(sync.pushRequests >= 3)
    }

    @Test
    fun addMissingSkipsIdsAlreadyThereEvenIfEditedOrDeleted() = runTest {
        val a = Fixtures.familyEvent("여름방학", day, id = "neis-a").copy(familyId = ""); val b = Fixtures.familyEvent("중간고사", day.plusDays(9), id = "neis-b").copy(familyId = "")
        assertEquals(2, repo.addMissing(listOf(a, b)))
        assertEquals(Fixtures.FAMILY, dao.getById("neis-a")!!.familyId)
        repo.save(dao.getById("neis-a")!!.copy(title = "여름방학 (할머니 댁)")); repo.delete("neis-b")
        assertEquals(0, repo.addMissing(listOf(a, b))) // 고친 것·지운 것은 다시 덮지 않음
        assertEquals("여름방학 (할머니 댁)", dao.getById("neis-a")!!.title); assertTrue(dao.getById("neis-b")!!.deleted)
    }
}
