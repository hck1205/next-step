package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.ActivityDao
import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.repository.ActivityRepository
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomActivityRepository(
    private val dao: ActivityDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), ActivityRepository {

    override val activities: Flow<List<ActivityEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun save(activity: ActivityEntity) {
        val title = activity.title.trim()
        if (title.isEmpty()) return
        val existing = dao.getById(activity.id)
        val author = existing?.createdByRole?.ifEmpty { null } ?: scope.currentProfile().role?.name ?: ""
        dao.upsert(
            activity.copy(
                familyId = familyIdOr(activity.familyId), title = title, place = activity.place.trim(), note = activity.note.trim(),
                rating = activity.rating.coerceIn(0, MAX_RATING), endDate = activity.endDate?.takeIf { it >= activity.date },
                createdByRole = author, updatedAt = now(), dirty = true,
            ),
        )
        pushLater()
    }

    override suspend fun delete(id: String) {
        val item = dao.getById(id) ?: return
        dao.upsert(item.copy(deleted = true, updatedAt = now(), dirty = true))
        pushLater()
    }

    private companion object {
        const val MAX_RATING = 5
    }
}
