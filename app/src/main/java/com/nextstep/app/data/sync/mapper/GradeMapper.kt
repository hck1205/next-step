package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.GradeEntity
import com.nextstep.app.data.model.ExamType
import com.nextstep.app.data.sync.EntityMapper

object GradeMapper : EntityMapper<GradeEntity> {
    override val collection = "grades"

    override fun toMap(entity: GradeEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "subjectId" to subjectId, "title" to title, "examType" to examType.name, "score" to score,
            "maxScore" to maxScore, "classAverage" to classAverage, "date" to date, "memo" to memo, "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): GradeEntity = GradeEntity(
        id = id, familyId = data.str("familyId"), subjectId = data.str("subjectId"), title = data.str("title"),
        examType = ExamType.from(data.strOrNull("examType")), score = data.dbl("score"), maxScore = data.dbl("maxScore", 100.0),
        classAverage = data.dblOrNull("classAverage"), date = data.long("date"), memo = data.str("memo"),
        updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )
}
