package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects", indices = [Index("familyId")])
data class SubjectEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val name: String,
    /** ARGB 색상 값. 차트/캘린더에서 과목 식별에 사용. */
    val color: Long,
    val teacher: String = "",
    /** 주간 학습 목표(분). 0이면 목표 없음. */
    val weeklyGoalMinutes: Int = 0,
    val orderIndex: Int = 0,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
