package com.nextstep.app.data.repository.room

import com.nextstep.app.data.local.dao.TopicDao
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.repository.FamilyScope
import com.nextstep.app.data.repository.TimeSource
import com.nextstep.app.data.repository.TopicRepository
import com.nextstep.app.data.repository.scopedList
import com.nextstep.app.data.sync.SyncManager
import kotlinx.coroutines.flow.Flow

class RoomTopicRepository(
    private val dao: TopicDao,
    scope: FamilyScope,
    sync: SyncManager,
    time: TimeSource,
) : SyncedWriter(scope, sync, time), TopicRepository {

    override val topics: Flow<List<TopicEntity>> = scope.scopedList { dao.observeAll(it) }

    override fun observeBySubject(subjectId: String): Flow<List<TopicEntity>> = dao.observeBySubject(subjectId)

    override suspend fun add(subjectId: String, titles: List<String>) {
        val clean = titles.map { it.trim() }.filter { it.isNotEmpty() }
        if (clean.isEmpty()) return
        val familyId = scope.requireFamilyId()
        val start = dao.getBySubject(subjectId).size
        dao.upsertAll(clean.mapIndexed { i, title -> TopicEntity(familyId = familyId, subjectId = subjectId, title = title, orderIndex = start + i, updatedAt = now()) })
        pushLater()
    }

    override suspend fun update(topic: TopicEntity) {
        dao.upsert(topic.copy(updatedAt = now(), dirty = true))
        pushLater()
    }

    override suspend fun setStatus(id: String, status: TopicStatus) {
        val topic = dao.getById(id) ?: return
        update(topic.copy(status = status))
    }

    override suspend fun setClassProgress(subjectId: String, upToOrderIndex: Int) {
        val ts = now()
        dao.upsertAll(
            dao.getBySubject(subjectId).map { topic ->
                val covered = topic.orderIndex <= upToOrderIndex
                // 예습 완료(PREVIEWED)는 보존해야 "수업 전 예습" 을 나중에 셀 수 있습니다.
                val status = if (covered && topic.status == TopicStatus.NOT_STARTED) TopicStatus.IN_CLASS else topic.status
                topic.copy(classCovered = covered, status = status, updatedAt = ts, dirty = true)
            },
        )
        pushLater()
    }

    override suspend fun delete(id: String) {
        val topic = dao.getById(id) ?: return
        update(topic.copy(deleted = true))
    }
}
