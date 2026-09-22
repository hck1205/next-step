package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.NoteEntity
import com.nextstep.app.data.sync.EntityMapper

object NoteMapper : EntityMapper<NoteEntity> {
    override val collection = "notes"

    override fun toMap(entity: NoteEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "authorRole" to authorRole, "authorName" to authorName, "text" to text,
            "createdAt" to createdAt, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): NoteEntity = NoteEntity(
        id = id, familyId = data.str("familyId"), authorRole = data.str("authorRole"), authorName = data.str("authorName"),
        text = data.str("text"), createdAt = data.long("createdAt"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
