package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.model.AptitudeDomain
import com.nextstep.app.data.sync.EntityMapper

object ObservationMapper : EntityMapper<ObservationEntity> {
    override val collection = "observations"

    override fun toMap(entity: ObservationEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "domain" to domain.name, "text" to text, "strength" to strength, "date" to date,
            "authorRole" to authorRole, "authorName" to authorName, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): ObservationEntity = ObservationEntity(
        id = id, familyId = data.str("familyId"), domain = AptitudeDomain.from(data.strOrNull("domain")), text = data.str("text"), strength = data.int("strength", 2),
        date = data.long("date"), authorRole = data.str("authorRole"), authorName = data.str("authorName"), updatedAt = data.long("updatedAt"),
        deleted = data.bool("deleted"), dirty = false,
    )
}
