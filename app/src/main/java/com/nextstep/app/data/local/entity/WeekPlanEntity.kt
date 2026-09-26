package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 한 주의 계획과 돌아보기(자기주도 한 바퀴). 주마다 한 행이고 [weekStart] 는 그 주 월요일(epochDay)입니다.
 * 누가 쓰고 확인하는지는 자기주도 단계(SelfDirectionStage)가 정합니다.
 */
@Entity(tableName = "week_plans", indices = [Index("familyId"), Index("weekStart")])
data class WeekPlanEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val weekStart: Long,
    /** 이번 주 목표. 한 줄에 하나, 3개까지. */
    val goals: String = "",
    /** 끝낸 목표(비트 i = goals 의 i번째). */
    val doneMask: Int = 0,
    /** 계획한 공부 시간(분). 어린 단계는 0. */
    val plannedMinutes: Int = 0,
    /** 계획을 쓴 사람의 역할(Role.name). */
    val authorRole: String = "",
    /** 어른이 확인한 때(아이가 먼저 계획하는 단계). */
    val approvedAt: Long? = null,
    /** 돌아보기 기분: 0 없음, 1 힘들었어요, 2 보통, 3 좋았어요. */
    val mood: Int = 0,
    val good: String = "",
    val hard: String = "",
    val change: String = "",
    val reflectedByRole: String = "",
    val reflectedAt: Long? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    val goalList: List<String> get() = goals.lines().map { it.trim() }.filter { it.isNotEmpty() }
    fun isDone(index: Int): Boolean = doneMask and (1 shl index) != 0
    val doneCount: Int get() = goalList.indices.count { isDone(it) }
    val isReflected: Boolean get() = reflectedAt != null
    val hasPlan: Boolean get() = goalList.isNotEmpty() || plannedMinutes > 0
}
