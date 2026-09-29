package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 멘토의 로드맵 템플릿(여러 학생에게 복사). 멘토 기기에만 두는 개인 서랍이라 가족 동기화에 올리지 않습니다.
 * [items] 는 RoadmapTemplates.encode 로 적은 항목들.
 */
@Entity(tableName = "roadmap_templates")
data class RoadmapTemplateEntity(
    @PrimaryKey val id: String = newId(),
    val name: String,
    /** 템플릿을 만든 로드맵의 주 과목 이름(불러올 때 그 학생의 같은 이름 과목에 붙임). */
    val subjectName: String = "",
    val items: String,
    val count: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)
