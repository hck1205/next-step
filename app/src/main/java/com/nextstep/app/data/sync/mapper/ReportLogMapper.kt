package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.ReportLogEntity
import com.nextstep.app.data.sync.EntityMapper

object ReportLogMapper : EntityMapper<ReportLogEntity> {
    override val collection = "reportLogs"

    override fun toMap(entity: ReportLogEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "kind" to kind, "title" to title, "sentById" to sentById, "sentByName" to sentByName,
            "sentAt" to sentAt, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): ReportLogEntity = ReportLogEntity(
        id = id, familyId = data.str("familyId"), kind = data.str("kind"), title = data.str("title"), sentById = data.str("sentById"),
        sentByName = data.str("sentByName"), sentAt = data.long("sentAt"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
