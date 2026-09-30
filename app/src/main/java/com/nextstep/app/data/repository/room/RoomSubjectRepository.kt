package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.SubjectDao
import com.nextstep.app.data.local.dao.TopicDao
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.SubjectRepository
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomSubjectRepository(
    private val dao: SubjectDao,
    private val topicDao: TopicDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), SubjectRepository {

    override val subjects: Flow<List<SubjectEntity>> = scope.scopedList { dao.observeAll(it) }

    override fun observe(id: String): Flow<SubjectEntity?> = dao.observeById(id)

    override suspend fun save(subject: SubjectEntity) {
        dao.upsert(subject.copy(familyId = familyIdOr(subject.familyId), updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun delete(id: String) {
        val subject = dao.getById(id) ?: return
        val ts = now()
        dao.upsert(subject.copy(deleted = true, updatedAt = ts, dirty = true))
        topicDao.upsertAll(topicDao.getBySubject(id).map { it.copy(deleted = true, updatedAt = ts, dirty = true) })
        pushLater()
    }
}
