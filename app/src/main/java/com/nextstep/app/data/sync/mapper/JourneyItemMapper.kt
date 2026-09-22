package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.JourneyItemEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.data.sync.EntityMapper

object JourneyItemMapper : EntityMapper<JourneyItemEntity> {
    override val collection = "journey"

    override fun toMap(entity: JourneyItemEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "templateId" to templateId, "title" to title, "description" to description,
            "category" to category, "dueDate" to dueDate, "leadMonths" to leadMonths, "priority" to priority, "status" to status.name,
            "note" to note, "doneAt" to doneAt, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): JourneyItemEntity = JourneyItemEntity(
        id = id, familyId = data.str("familyId"), templateId = data.strOrNull("templateId"), title = data.str("title"),
        description = data.str("description"), category = data.str("category"), dueDate = data.long("dueDate"),
        leadMonths = data.int("leadMonths", 1), priority = data.int("priority", 2), status = MilestoneStatus.from(data.strOrNull("status")),
        note = data.str("note"), doneAt = data.longOrNull("doneAt"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
