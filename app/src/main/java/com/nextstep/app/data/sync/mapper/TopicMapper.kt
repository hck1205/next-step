package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus
import com.nextstep.app.data.sync.EntityMapper

object TopicMapper : EntityMapper<TopicEntity> {
    override val collection = "topics"

    override fun toMap(entity: TopicEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "subjectId" to subjectId, "title" to title, "orderIndex" to orderIndex,
            "classCovered" to classCovered, "status" to status.name, "confidence" to confidence, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): TopicEntity = TopicEntity(
        id = id, familyId = data.str("familyId"), subjectId = data.str("subjectId"), title = data.str("title"),
        orderIndex = data.int("orderIndex"), classCovered = data.bool("classCovered"), status = TopicStatus.from(data.strOrNull("status")),
        confidence = data.int("confidence"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
