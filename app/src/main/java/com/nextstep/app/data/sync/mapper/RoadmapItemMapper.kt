package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.RoadmapItemEntity
import com.nextstep.app.data.model.RoadmapStatus
import com.nextstep.app.data.sync.EntityMapper

object RoadmapItemMapper : EntityMapper<RoadmapItemEntity> {
    override val collection = "roadmap"

    override fun toMap(entity: RoadmapItemEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "subjectId" to subjectId, "title" to title, "description" to description, "resource" to resource,
            "contentId" to contentId, "targetDate" to targetDate, "orderIndex" to orderIndex, "status" to status.name,
            "createdByName" to createdByName, "createdByRole" to createdByRole, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): RoadmapItemEntity = RoadmapItemEntity(
        id = id, familyId = data.str("familyId"), subjectId = data.strOrNull("subjectId"), title = data.str("title"),
        description = data.str("description"), resource = data.str("resource"), contentId = data.strOrNull("contentId"),
        targetDate = data.longOrNull("targetDate"), orderIndex = data.int("orderIndex"), status = RoadmapStatus.from(data.strOrNull("status")),
        createdByName = data.str("createdByName"), createdByRole = data.str("createdByRole"),
        updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
