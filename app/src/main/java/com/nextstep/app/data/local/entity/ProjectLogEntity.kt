package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 교육 프로젝트 루틴 한 번의 기록: 어느 프로젝트(goalId)의 어느 단계(phaseKey)에서 무엇을(item) 몇 분 했는지.
 * 공부 시간(StudySessionEntity)과 따로 두어 운동·악기 같은 루틴이 과목 공부 시간에 섞이지 않게 합니다.
 */
@Entity(tableName = "project_logs", indices = [Index("familyId"), Index("goalId")])
data class ProjectLogEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val goalId: String,
    /** ProjectPhase.key */
    val phaseKey: String,
    /** RoutineItem.name */
    val item: String,
    val minutes: Int,
    /** 한 날짜(epochDay). */
    val date: Long,
    val authorRole: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
