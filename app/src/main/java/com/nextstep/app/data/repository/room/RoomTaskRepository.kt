package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.TaskDao
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TaskRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomTaskRepository(
    private val dao: TaskDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), TaskRepository {

    override val tasks: Flow<List<TaskEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun save(task: TaskEntity) {
        dao.upsert(task.copy(familyId = familyIdOr(task.familyId), updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun setDone(id: String, done: Boolean) {
        val task = dao.getById(id) ?: return
        if (task.done == done) return
        save(task.copy(done = done, doneAt = if (done) now() else null))
    }

    override suspend fun delete(id: String) {
        val task = dao.getById(id) ?: return
        save(task.copy(deleted = true))
    }
}
