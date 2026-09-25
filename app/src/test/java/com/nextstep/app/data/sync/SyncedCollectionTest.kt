package com.nextstep.app.data.sync

import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.sync.mapper.TaskMapper
import com.nextstep.app.fake.dao.FakeTaskDao
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncedCollectionTest {
    private val store = mutableMapOf<String, TaskEntity>()
    private val cleaned = mutableListOf<String>()

    private val collection = SyncedCollection(
        mapper = TaskMapper,
        getLocal = { store[it] },
        upsert = { store[it.id] = it },
        getDirty = { fam -> store.values.filter { it.familyId == fam && it.dirty } },
        markClean = { ids -> cleaned += ids; ids.forEach { id -> store[id] = store.getValue(id).copy(dirty = false) } },
    )

    private fun doc(id: String, updatedAt: Long, dirty: Boolean = true, text: String = "t") =
        TaskEntity(id = id, familyId = "fam", title = text, dueDate = 0, createdByRole = "PARENT", updatedAt = updatedAt, dirty = dirty)

    @Test
    fun mergeRemoteAppliesOnlyNewerDocuments() = runTest {
        store["n1"] = doc("n1", updatedAt = 100, text = "local")
        val stale = TaskMapper.toMap(doc("n1", updatedAt = 50, text = "old remote"))
        val newer = TaskMapper.toMap(doc("n1", updatedAt = 200, text = "new remote"))

        assertFalse(collection.mergeRemote("n1", stale))
        assertEquals("local", store.getValue("n1").title)
        assertTrue(collection.mergeRemote("n1", newer))
        assertEquals("new remote", store.getValue("n1").title)
        assertFalse(store.getValue("n1").dirty)
    }

    @Test
    fun reconcileCanPreserveLocalOnlyFields() = runTest {
        val preserving = SyncedCollection(
            mapper = TaskMapper,
            getLocal = { store[it] },
            upsert = { store[it.id] = it },
            getDirty = { emptyList() },
            markClean = {},
            reconcile = { remote, local -> remote.copy(title = local?.title ?: remote.title) },
        )
        store["n1"] = doc("n1", updatedAt = 1, text = "keep me")

        assertTrue(preserving.mergeRemote("n1", TaskMapper.toMap(doc("n1", updatedAt = 2, text = "remote"))))
        assertEquals("keep me", store.getValue("n1").title)
        assertEquals(2L, store.getValue("n1").updatedAt)
    }

    @Test
    fun pushDirtySendsInBatchesAndMarksClean() = runTest {
        (1..5).forEach { store["n$it"] = doc("n$it", updatedAt = it.toLong()) }
        store["other"] = doc("other", updatedAt = 9).copy(familyId = "elsewhere")
        val batches = mutableListOf<Int>()

        collection.pushDirty("fam", batchSize = 2) { docs -> batches += docs.size }

        assertEquals(listOf(2, 2, 1), batches)
        assertEquals(5, cleaned.size)
        assertTrue(store.values.filter { it.familyId == "fam" }.none { it.dirty })
        assertTrue(store.getValue("other").dirty)
    }

    @Test
    fun factoryBindsDaoContractAndReadOnlyNeverPushes() = runTest {
        val dao = FakeTaskDao()
        val viaDao = SyncedCollection.of(TaskMapper, dao)
        assertTrue(viaDao.mergeRemote("n1", TaskMapper.toMap(doc("n1", updatedAt = 10))))
        assertEquals("t", dao.getById("n1")!!.title)
        assertFalse(dao.getById("n1")!!.dirty)
        dao.upsert(doc("n2", updatedAt = 20))
        val pushed = mutableListOf<String>()
        viaDao.pushDirty("fam") { docs -> pushed += docs.map { it.first } }
        assertEquals(listOf("n2"), pushed)

        val readOnly = SyncedCollection.readOnly(TaskMapper, dao::getById, dao::upsert)
        var pushedRo = 0
        readOnly.pushDirty("fam") { pushedRo += it.size }
        assertEquals(0, pushedRo)
        assertEquals(TaskMapper.collection, readOnly.name)
    }
}
