package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.ActivityEntity
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.data.sync.EntityMapper

object ActivityMapper : EntityMapper<ActivityEntity> {
    override val collection = "activities"

    override fun toMap(entity: ActivityEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "type" to type.name, "title" to title, "date" to date, "endDate" to endDate, "place" to place,
            "note" to note, "rating" to rating, "createdByRole" to createdByRole, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): ActivityEntity = ActivityEntity(
        id = id, familyId = data.str("familyId"), type = ActivityType.from(data.strOrNull("type")), title = data.str("title"), date = data.long("date"),
        endDate = data.longOrNull("endDate"), place = data.str("place"), note = data.str("note"), rating = data.int("rating"),
        createdByRole = data.str("createdByRole"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
