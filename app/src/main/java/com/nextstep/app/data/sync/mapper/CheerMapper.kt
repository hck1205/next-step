package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.CheerEntity
import com.nextstep.app.data.sync.EntityMapper

object CheerMapper : EntityMapper<CheerEntity> {
    override val collection = "cheers"

    override fun toMap(entity: CheerEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "taskId" to taskId, "taskTitle" to taskTitle, "kind" to kind, "fromId" to fromId, "fromRole" to fromRole,
            "fromName" to fromName, "seenAt" to seenAt, "createdAt" to createdAt, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): CheerEntity = CheerEntity(
        id = id, familyId = data.str("familyId"), taskId = data.str("taskId"), taskTitle = data.str("taskTitle"), kind = data.str("kind"),
        fromId = data.str("fromId"), fromRole = data.str("fromRole"), fromName = data.str("fromName"), seenAt = data.longOrNull("seenAt"),
        createdAt = data.long("createdAt"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
