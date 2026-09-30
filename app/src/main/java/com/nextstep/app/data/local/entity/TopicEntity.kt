package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.TopicStatus

@Entity(tableName = "topics", indices = [Index("familyId"), Index("subjectId")])
data class TopicEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val subjectId: String,
    val title: String,
    val orderIndex: Int,
    /** 학급(수업)에서 이미 다룬 단원인지. 학급 진도를 나타냅니다. */
    val classCovered: Boolean = false,
    /** 학생 본인의 학습 상태. */
    val status: TopicStatus = TopicStatus.NOT_STARTED,
    /** 자기 평가 이해도 0~100. */
    val confidence: Int = 0,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    /** 수업했는데 아직 복습하지 않은 단원(복습 대기열). */
    val needsReview: Boolean get() = classCovered && status.order < TopicStatus.REVIEWED.order

    /** 수업 전인데 아직 예습하지 않은 단원(예습 대기열). */
    val needsPreview: Boolean get() = !classCovered && status.order < TopicStatus.PREVIEWED.order
}
