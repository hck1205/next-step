package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.StudySessionEntity
import com.nextstep.app.data.sync.EntityMapper

object StudySessionMapper : EntityMapper<StudySessionEntity> {
    override val collection = "sessions"

    override fun toMap(entity: StudySessionEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "subjectId" to subjectId, "startAt" to startAt, "endAt" to endAt,
            "durationMinutes" to durationMinutes, "note" to note, "fromTimer" to fromTimer, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): StudySessionEntity = StudySessionEntity(
        id = id, familyId = data.str("familyId"), subjectId = data.strOrNull("subjectId"), startAt = data.long("startAt"),
        endAt = data.long("endAt"), durationMinutes = data.int("durationMinutes"), note = data.str("note"), fromTimer = data.bool("fromTimer"),
        updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
