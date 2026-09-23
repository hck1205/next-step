package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.GrowthRecordEntity
import com.nextstep.app.data.sync.EntityMapper

object GrowthRecordMapper : EntityMapper<GrowthRecordEntity> {
    override val collection = "growth"

    override fun toMap(entity: GrowthRecordEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "date" to date, "heightCm" to heightCm, "weightKg" to weightKg, "visionLeft" to visionLeft,
            "visionRight" to visionRight, "note" to note, "createdByRole" to createdByRole, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): GrowthRecordEntity = GrowthRecordEntity(
        id = id, familyId = data.str("familyId"), date = data.long("date"), heightCm = data.dblOrNull("heightCm"), weightKg = data.dblOrNull("weightKg"),
        visionLeft = data.dblOrNull("visionLeft"), visionRight = data.dblOrNull("visionRight"), note = data.str("note"), createdByRole = data.str("createdByRole"),
        updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
