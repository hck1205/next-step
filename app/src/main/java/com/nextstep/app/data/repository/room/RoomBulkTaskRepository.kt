package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.SubjectDao
import com.nextstep.app.data.local.dao.TaskDao
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.local.entity.newId
import com.nextstep.app.data.repository.BulkTaskRepository
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.sync.SyncManager

class RoomBulkTaskRepository(
    private val tasks: TaskDao,
    private val subjects: SubjectDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), BulkTaskRepository {

    override suspend fun assign(task: TaskEntity, familyIds: List<String>, subjectName: String?): Int {
        if (task.title.isBlank()) return 0
        val families = familyIds.distinct()
        families.forEach { familyId ->
            val subject = subjectName?.let { name -> subjects.getAll(familyId).firstOrNull { it.name == name } }
            tasks.upsert(task.copy(id = newId(), familyId = familyId, subjectId = subject?.id, updatedAt = now(), dirty = true))
        }
        pushLater()
        return families.size
    }
}
