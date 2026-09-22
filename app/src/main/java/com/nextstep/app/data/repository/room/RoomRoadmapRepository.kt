package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.RoadmapDao
import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.RoadmapRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomRoadmapRepository(
    private val dao: RoadmapDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), RoadmapRepository {

    override val roadmap: Flow<List<RoadmapItemEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun save(item: RoadmapItemEntity) {
        val authored = if (item.createdByName.isNotBlank()) item else scope.currentProfile().let { p ->
            item.copy(createdByName = p.displayName, createdByRole = p.role?.name ?: "")
        }
        dao.upsert(authored.copy(familyId = familyIdOr(authored.familyId), updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun setStatus(id: String, status: RoadmapStatus) {
        val item = dao.getById(id) ?: return
        save(item.copy(status = status))
    }

    override suspend fun delete(id: String) {
        val item = dao.getById(id) ?: return
        save(item.copy(deleted = true))
    }
}
