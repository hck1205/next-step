package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 학부모·멘토가 약속한 보상 하나: 무엇을 이루면([kind] + [targetId]) 무엇을 줄지([title]).
 * 이룬 여부는 목표·레벨에서 계산하고(RewardKind), 준 때만 여기 남깁니다([givenAt]).
 */
@Entity(tableName = "rewards", indices = [Index("familyId")])
data class RewardEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    /** RewardKind.name: GOAL(목표 id) 또는 LEVEL(레벨 번호). */
    val kind: String,
    val targetId: String,
    val title: String,
    val createdByRole: String = "",
    val givenAt: Long? = null,
    val givenByRole: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
