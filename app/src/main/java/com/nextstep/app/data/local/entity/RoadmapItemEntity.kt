package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.RoadmapStatus

/**
 * 멘토(또는 학부모 겸 멘토)가 큐레이팅한 학습 로드맵 항목.
 * "이 순서로, 이 자료로, 이 날짜까지" 를 제안하고 학생이 진행 상태를 갱신합니다.
 */
@Entity(tableName = "roadmap_items", indices = [Index("familyId"), Index("subjectId")])
data class RoadmapItemEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String? = null,
    val title: String,
    val description: String = "",
    /** 참고 자료 링크나 교재명. */
    val resource: String = "",
    /** 콘텐츠 라이브러리 항목을 연결한 경우 그 ID. */
    val contentId: String? = null,
    /** 목표일 (epoch day). null 이면 기한 없음. */
    val targetDate: Long? = null,
    val orderIndex: Int = 0,
    val status: RoadmapStatus = RoadmapStatus.PLANNED,
    val createdByName: String = "",
    val createdByRole: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
