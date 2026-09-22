package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.model.EventType
import com.nextstep.app.data.sync.EntityMapper

object EventMapper : EntityMapper<EventEntity> {
    override val collection = "events"

    override fun toMap(entity: EventEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "subjectId" to subjectId, "title" to title, "type" to type.name, "startAt" to startAt,
            "endAt" to endAt, "repeatWeekly" to repeatWeekly, "location" to location, "memo" to memo, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): EventEntity = EventEntity(
        id = id, familyId = data.str("familyId"), subjectId = data.strOrNull("subjectId"), title = data.str("title"),
        type = EventType.from(data.strOrNull("type")), startAt = data.long("startAt"), endAt = data.long("endAt"),
        repeatWeekly = data.bool("repeatWeekly"), location = data.str("location"), memo = data.str("memo"),
        updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
