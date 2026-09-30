package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.LessonEntity
import com.nextstep.app.data.sync.EntityMapper

object LessonMapper : EntityMapper<LessonEntity> {
    override val collection = "lessons"

    override fun toMap(entity: LessonEntity): Map<String, Any?> = with(entity) {
        syncFields() + mapOf("mentorId" to mentorId, "date" to date, "status" to status, "note" to note)
    }

    override fun fromMap(id: String, data: Map<String, Any?>): LessonEntity = LessonEntity(
        id = id, familyId = data.str("familyId"), mentorId = data.str("mentorId"), date = data.long("date"), status = data.str("status"),
        note = data.str("note"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
