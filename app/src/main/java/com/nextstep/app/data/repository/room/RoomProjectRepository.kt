package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.ProjectLogDao
import com.nextstep.app.data.local.entity.ProjectLogEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.ProjectRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomProjectRepository(
    private val dao: ProjectLogDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), ProjectRepository {

    override val logs: Flow<List<ProjectLogEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun log(goalId: String, phaseKey: String, item: String, minutes: Int, date: Long) {
        if (minutes <= 0 || goalId.isBlank()) return
        dao.upsert(
            ProjectLogEntity(
                familyId = familyIdOr(""), goalId = goalId, phaseKey = phaseKey, item = item.trim(), minutes = minutes.coerceAtMost(MAX_MINUTES),
                date = date, authorRole = scope.currentProfile().role?.name ?: "", updatedAt = now(), dirty = true,
            ),
        )
        pushLater()
    }

    override suspend fun toggle(goalId: String, phaseKey: String, item: String, minutes: Int, date: Long) {
        val existing = dao.findOn(goalId, item.trim(), date)
        if (existing.isEmpty()) return log(goalId, phaseKey, item, minutes, date)
        existing.forEach { dao.upsert(it.copy(deleted = true, updatedAt = now(), dirty = true)) }
        pushLater()
    }

    override suspend fun delete(id: String) {
        val item = dao.getById(id) ?: return
        dao.upsert(item.copy(deleted = true, updatedAt = now(), dirty = true))
        pushLater()
    }

    private companion object {
        /** 한 번 기록의 상한(실수로 큰 값을 넣지 않게). */
        const val MAX_MINUTES = 240
    }
}
