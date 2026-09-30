package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.CheerDao
import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.repository.CheerRepository
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import com.nextstep.app.domain.cheer.CheerKind
import kotlinx.coroutines.flow.Flow

class RoomCheerRepository(
    private val dao: CheerDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), CheerRepository {

    override val cheers: Flow<List<CheerEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun set(task: TaskEntity, kind: CheerKind?, fromName: String) {
        val me = scope.currentProfile()
        val familyId = familyIdOr(task.familyId)
        val fromId = me.memberId.orEmpty()
        val old = dao.findMine(familyId, task.id, fromId).firstOrNull()
        val now = now()
        when {
            kind == null -> old?.let { dao.upsert(it.copy(deleted = true, updatedAt = now, dirty = true)) }
            // 바꾸면 아이에게 다시 보이도록 seenAt 을 비웁니다.
            old != null -> dao.upsert(old.copy(kind = kind.name, fromName = fromName, seenAt = null, updatedAt = now, dirty = true))
            else -> dao.upsert(
                CheerEntity(
                    familyId = familyId, taskId = task.id, taskTitle = task.title, kind = kind.name, fromId = fromId,
                    fromRole = me.role?.name.orEmpty(), fromName = fromName, createdAt = now, updatedAt = now,
                ),
            )
        }
        pushLater()
    }

    override suspend fun markSeen(ids: List<String>) {
        val now = now()
        ids.mapNotNull { dao.getById(it) }.filter { it.seenAt == null }.forEach { dao.upsert(it.copy(seenAt = now, updatedAt = now, dirty = true)) }
        pushLater()
    }
}
