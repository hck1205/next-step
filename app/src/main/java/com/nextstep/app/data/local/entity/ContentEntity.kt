package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.ContentScope
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel

/**
 * 교육 콘텐츠(현재는 유튜브 링크). 영상 자체는 저장하지 않고 링크와 메타데이터, 분류만 저장합니다.
 * 등록 시 제목·채널을 가져와 규칙 기반으로 과목/학년/유형/키워드를 붙이고, 이 분류가 큐레이팅과 추천의 재료가 됩니다.
 */
@Entity(tableName = "contents", indices = [Index("familyId"), Index("subjectKey"), Index("scope")])
data class ContentEntity(
    @PrimaryKey override val id: String = newId(),
    /** GLOBAL 콘텐츠는 "global" 고정. */
    override val familyId: String,
    val scope: ContentScope = ContentScope.FAMILY,
    val url: String,
    /** 유튜브 영상 ID. 중복 등록 방지와 썸네일에 사용. */
    val videoId: String = "",
    val title: String,
    val channel: String = "",
    val thumbnailUrl: String = "",
    /** 과목 키(예: "수학"). 가족 과목 ID 가 아니라 이름 기반이라 GLOBAL 콘텐츠와도 맞춰집니다. */
    val subjectKey: String = "",
    val gradeLevel: GradeLevel = GradeLevel.ALL,
    val contentType: ContentType = ContentType.OTHER,
    /** 쉼표로 구분한 키워드(단원명, 주제). 추천 시 단원 제목과 매칭. */
    val keywords: String = "",
    /** 등록자가 남긴 한 줄 설명 또는 자동 요약. */
    val summary: String = "",
    val durationMinutes: Int = 0,
    val ratingSum: Int = 0,
    val ratingCount: Int = 0,
    /** 학생이 시청 완료 표시. */
    val watched: Boolean = false,
    val createdByName: String = "",
    val createdByRole: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    val keywordList: List<String> get() = keywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val averageRating: Float get() = if (ratingCount == 0) 0f else ratingSum.toFloat() / ratingCount

    companion object {
        const val GLOBAL_FAMILY = "global"
    }
}
