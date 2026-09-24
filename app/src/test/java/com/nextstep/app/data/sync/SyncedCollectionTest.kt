package com.nextstep.app.data.sync

import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.sync.mapper.NoteMapper
import com.nextstep.app.fake.dao.FakeNoteDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncedCollectionTest {
    private val store = mutableMapOf<String, NoteEntity>()
    private val cleaned = mutableListOf<String>()

    private val collection = SyncedCollection(
        mapper = NoteMapper,
        getLocal = { store[it] },
        upsert = { store[it.id] = it },
        getDirty = { fam -> store.values.filter { it.familyId == fam && it.dirty } },
        markClean = { ids -> cleaned += ids; ids.forEach { id -> store[id] = store.getValue(id).copy(dirty = false) } },
    )

    private fun note(id: String, updatedAt: Long, dirty: Boolean = true, text: String = "t") =
        NoteEntity(id = id, familyId = "fam", authorRole = "PARENT", authorName = "a", text = text, updatedAt = updatedAt, dirty = dirty)

    @Test
    fun mergeRemoteAppliesOnlyNewerDocuments() = runTest {
        store["n1"] = note("n1", updatedAt = 100, text = "local")
        val stale = NoteMapper.toMap(note("n1", updatedAt = 50, text = "old remote"))
        val newer = NoteMapper.toMap(note("n1", updatedAt = 200, text = "new remote"))

        assertFalse(collection.mergeRemote("n1", stale))
        assertEquals("local", store.getValue("n1").text)
        assertTrue(collection.mergeRemote("n1", newer))
        assertEquals("new remote", store.getValue("n1").text)
        assertFalse(store.getValue("n1").dirty)
    }

    @Test
    fun reconcileCanPreserveLocalOnlyFields() = runTest {
        val preserving = SyncedCollection(
            mapper = NoteMapper,
            getLocal = { store[it] },
            upsert = { store[it.id] = it },
            getDirty = { emptyList() },
            markClean = {},
            reconcile = { remote, local -> remote.copy(text = local?.text ?: remote.text) },
        )
        store["n1"] = note("n1", updatedAt = 1, text = "keep me")

        assertTrue(preserving.mergeRemote("n1", NoteMapper.toMap(note("n1", updatedAt = 2, text = "remote"))))
        assertEquals("keep me", store.getValue("n1").text)
        assertEquals(2L, store.getValue("n1").updatedAt)
    }

    @Test
    fun pushDirtySendsInBatchesAndMarksClean() = runTest {
        (1..5).forEach { store["n$it"] = note("n$it", updatedAt = it.toLong()) }
        store["other"] = note("other", updatedAt = 9).copy(familyId = "elsewhere")
        val batches = mutableListOf<Int>()

        collection.pushDirty("fam", batchSize = 2) { docs -> batches += docs.size }

        assertEquals(listOf(2, 2, 1), batches)
        assertEquals(5, cleaned.size)
        assertTrue(store.values.filter { it.familyId == "fam" }.none { it.dirty })
        assertTrue(store.getValue("other").dirty)
    }

    @Test
    fun factoryBindsDaoContractAndReadOnlyNeverPushes() = runTest {
        val dao = FakeNoteDao()
        val viaDao = SyncedCollection.of(NoteMapper, dao)
        assertTrue(viaDao.mergeRemote("n1", NoteMapper.toMap(note("n1", updatedAt = 10))))
        assertEquals("t", dao.getById("n1")!!.text)
        assertFalse(dao.getById("n1")!!.dirty)
        dao.upsert(note("n2", updatedAt = 20))
        val pushed = mutableListOf<String>()
        viaDao.pushDirty("fam") { docs -> pushed += docs.map { it.first } }
        assertEquals(listOf("n2"), pushed)

        val readOnly = SyncedCollection.readOnly(NoteMapper, dao::getById, dao::upsert)
        var pushedRo = 0
        readOnly.pushDirty("fam") { pushedRo += it.size }
        assertEquals(0, pushedRo)
        assertEquals(NoteMapper.collection, readOnly.name)
    }
}
