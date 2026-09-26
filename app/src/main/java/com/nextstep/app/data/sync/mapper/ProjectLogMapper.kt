package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.ProjectLogEntity
import com.nextstep.app.data.sync.EntityMapper

object ProjectLogMapper : EntityMapper<ProjectLogEntity> {
    override val collection = "projectLogs"

    override fun toMap(entity: ProjectLogEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "goalId" to goalId, "phaseKey" to phaseKey, "item" to item, "minutes" to minutes,
            "date" to date, "authorRole" to authorRole, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): ProjectLogEntity = ProjectLogEntity(
        id = id, familyId = data.str("familyId"), goalId = data.str("goalId"), phaseKey = data.str("phaseKey"), item = data.str("item"),
        minutes = data.int("minutes"), date = data.long("date"), authorRole = data.str("authorRole"), updatedAt = data.long("updatedAt"),
        deleted = data.bool("deleted"), dirty = false,
    )
}
