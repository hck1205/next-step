package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.GoalStatus

/**
 * 장기 목표. 트랙에서 시작했으면 trackId 가 있고, 직접 만든 목표는 null 입니다.
 * 단계는 [GoalStepEntity] 로 구간(학기)마다 하나씩 붙습니다.
 */
@Entity(tableName = "goals", indices = [Index("familyId")])
data class GoalEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val trackId: String? = null,
    val title: String,
    /** GoalArea.name */
    val area: String = "",
    val description: String = "",
    val status: GoalStatus = GoalStatus.ACTIVE,
    /** 시험·수행평가·입시처럼 날짜가 정해진 목표의 D-day(epochDay). 장기 목표는 null. */
    val targetDate: Long? = null,
    val createdByRole: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
