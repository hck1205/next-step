package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.FamilyEventDao
import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.data.repository.FamilyEventRepository
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomFamilyEventRepository(
    private val dao: FamilyEventDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), FamilyEventRepository {

    override val events: Flow<List<FamilyEventEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun save(event: FamilyEventEntity) {
        if (event.title.isBlank()) return
        val me = scope.currentProfile()
        val now = now()
        val isNew = dao.getById(event.id) == null
        dao.upsert(
            event.copy(
                familyId = familyIdOr(event.familyId), title = event.title.trim(),
                createdById = if (isNew) me.memberId.orEmpty() else event.createdById,
                createdByRole = if (isNew) me.role?.name.orEmpty() else event.createdByRole,
                createdAt = if (isNew) now else event.createdAt, updatedAt = now, dirty = true,
            ),
        )
        pushLater()
    }

    override suspend fun addMissing(events: List<FamilyEventEntity>): Int {
        val now = now()
        val fresh = events.filter { dao.getById(it.id) == null }
        fresh.forEach { dao.upsert(it.copy(familyId = familyIdOr(it.familyId), createdAt = now, updatedAt = now, dirty = true)) }
        if (fresh.isNotEmpty()) pushLater()
        return fresh.size
    }

    override suspend fun delete(id: String) {
        val e = dao.getById(id) ?: return
        dao.upsert(e.copy(deleted = true, updatedAt = now(), dirty = true))
        pushLater()
    }
}
