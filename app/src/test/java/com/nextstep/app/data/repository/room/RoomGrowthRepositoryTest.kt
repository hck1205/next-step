package com.nextstep.app.data.repository.room

import com.nextstep.app.data.model.AptitudeDomain
import com.nextstep.app.data.model.Role
import com.nextstep.app.fake.dao.FakeGrowthRecordDao
import com.nextstep.app.fake.dao.FakeObservationDao
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

class RoomGrowthRepositoryTest {
    private val records = FakeGrowthRecordDao()
    private val observations = FakeObservationDao()
    private val sync = RecordingSyncManager()
    private val repo = RoomGrowthRepository(records, observations, FakeFamilyScope(Role.PARENT, displayName = "엄마"), sync, FakeTimeSource(7L))
    private val day = LocalDate.of(2029, 10, 10)

    @Test
    fun recordsAreValidatedClampedAndSoftDeleted() = runTest {
        repo.saveRecord(Fixtures.growth(day))
        assertTrue(records.all.isEmpty())
        repo.saveRecord(Fixtures.growth(day, height = 130.0, weight = -3.0, visionL = 5.0, visionR = 0.8, note = " 검진 ").copy(familyId = ""))
        val row = records.all.single()
        assertEquals(130.0, row.heightCm!!, 0.0); assertNull(row.weightKg); assertEquals(2.0, row.visionLeft!!, 0.0); assertEquals(0.8, row.visionRight!!, 0.0)
        assertEquals("검진", row.note); assertEquals(Fixtures.FAMILY, row.familyId); assertEquals("PARENT", row.createdByRole); assertEquals(7L, row.updatedAt)
        RoomGrowthRepository(records, observations, FakeFamilyScope(Role.STUDENT), sync, FakeTimeSource()).saveRecord(row.copy(weightKg = 28.0))
        assertEquals("PARENT", records.getById(row.id)!!.createdByRole); assertEquals(28.0, records.getById(row.id)!!.weightKg!!, 0.0)
        repo.deleteRecord(row.id); repo.deleteRecord("nope")
        assertTrue(records.getById(row.id)!!.deleted); assertTrue(repo.records.first().isEmpty())
        assertEquals(3, sync.pushRequests)
    }

    @Test
    fun observationsStampAuthorAndClampStrength() = runTest {
        repo.addObservation(Fixtures.observation(AptitudeDomain.MUSIC, "   "))
        assertTrue(observations.all.isEmpty())
        repo.addObservation(Fixtures.observation(AptitudeDomain.MUSIC, " 따라 부른다 ", strength = 9).copy(familyId = ""))
        val row = observations.all.single()
        assertEquals("따라 부른다", row.text); assertEquals(3, row.strength); assertEquals("PARENT", row.authorRole); assertEquals("엄마", row.authorName); assertEquals(Fixtures.FAMILY, row.familyId)
        repo.addObservation(Fixtures.observation(AptitudeDomain.SPORT, "x", strength = 0, id = "o2"))
        assertEquals(1, observations.getById("o2")!!.strength)
        repo.deleteObservation(row.id)
        assertEquals(listOf("o2"), repo.observations.first().map { it.id })
    }
}
