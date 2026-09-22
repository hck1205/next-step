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
) : Syncable
