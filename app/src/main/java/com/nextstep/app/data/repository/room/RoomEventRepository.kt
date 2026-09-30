package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.EventDao
import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.repository.EventRepository
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomEventRepository(
    private val dao: EventDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), EventRepository {

    override val events: Flow<List<EventEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun save(event: EventEntity) {
        dao.upsert(event.copy(familyId = familyIdOr(event.familyId), updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun delete(id: String) {
        val event = dao.getById(id) ?: return
        save(event.copy(deleted = true))
    }
}
