package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.RewardDao
import com.nextstep.app.data.local.entity.RewardEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.RewardRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomRewardRepository(
    private val dao: RewardDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), RewardRepository {

    override val rewards: Flow<List<RewardEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun promise(kind: String, targetId: String, title: String) {
        val text = title.trim()
        if (text.isEmpty() || targetId.isBlank()) return
        val familyId = familyIdOr("")
        val role = scope.currentProfile().role?.name ?: ""
        val open = dao.findOpen(familyId, kind, targetId).firstOrNull()
        val now = now()
        dao.upsert(
            open?.copy(title = text, createdByRole = role, updatedAt = now, dirty = true)
                ?: RewardEntity(familyId = familyId, kind = kind, targetId = targetId, title = text, createdByRole = role, createdAt = now, updatedAt = now),
        )
        pushLater()
    }

    override suspend fun give(id: String) {
        val r = dao.getById(id) ?: return
        if (r.givenAt != null) return
        val now = now()
        dao.upsert(r.copy(givenAt = now, givenByRole = scope.currentProfile().role?.name ?: "", updatedAt = now, dirty = true))
        pushLater()
    }

    override suspend fun cancel(id: String) {
        val r = dao.getById(id) ?: return
        dao.upsert(r.copy(deleted = true, updatedAt = now(), dirty = true))
        pushLater()
    }
}
