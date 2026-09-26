package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.model.GoalStatus
import com.nextstep.app.data.sync.EntityMapper

object GoalMapper : EntityMapper<GoalEntity> {
    override val collection = "goals"

    override fun toMap(entity: GoalEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "trackId" to trackId, "title" to title, "area" to area, "description" to description,
            "status" to status.name, "targetDate" to targetDate, "createdByRole" to createdByRole, "leadsTo" to leadsTo, "doneAt" to doneAt, "createdAt" to createdAt, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): GoalEntity = GoalEntity(
        id = id, familyId = data.str("familyId"), trackId = data.strOrNull("trackId"), title = data.str("title"), area = data.str("area"),
        description = data.str("description"), status = GoalStatus.from(data.strOrNull("status")), targetDate = data.longOrNull("targetDate"), createdByRole = data.str("createdByRole"), leadsTo = data.strOrNull("leadsTo"), doneAt = data.longOrNull("doneAt"),
        createdAt = data.long("createdAt"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
