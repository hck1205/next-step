package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.GradeDao
import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.GradeRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomGradeRepository(
    private val dao: GradeDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), GradeRepository {

    override val grades: Flow<List<GradeEntity>> = scope.scopedList { dao.observeAll(it) }

    override suspend fun save(grade: GradeEntity) {
        dao.upsert(grade.copy(familyId = familyIdOr(grade.familyId), updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun delete(id: String) {
        val grade = dao.getById(id) ?: return
        save(grade.copy(deleted = true))
    }
}
