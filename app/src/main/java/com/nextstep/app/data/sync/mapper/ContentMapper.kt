package com.nextstep.app.data.sync.mapper

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.model.ContentScope
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.data.sync.EntityMapper

/**
 * 콘텐츠 매퍼. 같은 문서 형식을 가족 저장소(`contents`)와 공용 저장소(`catalog`)가 공유하므로
 * 범위(scope)만 다른 두 인스턴스를 씁니다.
 */
class ContentMapper private constructor(override val collection: String, private val scope: ContentScope) : EntityMapper<ContentEntity> {

    override fun toMap(entity: ContentEntity): Map<String, Any?> = with(entity) {
        mapOf(
            "id" to id, "familyId" to familyId, "scope" to scope.name, "url" to url, "videoId" to videoId, "title" to title, "channel" to channel,
            "thumbnailUrl" to thumbnailUrl, "subjectKey" to subjectKey, "gradeLevel" to gradeLevel.name, "contentType" to contentType.name,
            "keywords" to keywords, "summary" to summary, "durationMinutes" to durationMinutes, "ratingSum" to ratingSum, "ratingCount" to ratingCount,
            "watched" to watched, "createdByName" to createdByName, "createdByRole" to createdByRole, "createdAt" to createdAt,
            "updatedAt" to updatedAt, "deleted" to deleted,
        )
    }

    override fun fromMap(id: String, data: Map<String, Any?>): ContentEntity = ContentEntity(
        id = id, familyId = if (scope == ContentScope.GLOBAL) ContentEntity.GLOBAL_FAMILY else data.str("familyId"), scope = scope,
        url = data.str("url"), videoId = data.str("videoId"), title = data.str("title"), channel = data.str("channel"),
        thumbnailUrl = data.str("thumbnailUrl"), subjectKey = data.str("subjectKey"), gradeLevel = GradeLevel.from(data.strOrNull("gradeLevel")),
        contentType = ContentType.from(data.strOrNull("contentType")), keywords = data.str("keywords"), summary = data.str("summary"),
        durationMinutes = data.int("durationMinutes"), ratingSum = data.int("ratingSum"), ratingCount = data.int("ratingCount"),
        watched = data.bool("watched"), createdByName = data.str("createdByName"), createdByRole = data.str("createdByRole"),
        createdAt = data.long("createdAt"), updatedAt = data.long("updatedAt"), deleted = data.bool("deleted"), dirty = false,
    )

    companion object {
        val Family = ContentMapper("contents", ContentScope.FAMILY)
        val Catalog = ContentMapper("catalog", ContentScope.GLOBAL)
    }
}
