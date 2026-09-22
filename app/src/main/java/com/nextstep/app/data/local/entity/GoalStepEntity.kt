package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.MilestoneStatus

/** 목표의 구간(학기)별 단계. 할 일로 보내면 taskId 가 연결됩니다. */
@Entity(tableName = "goal_steps", indices = [Index("familyId"), Index("goalId")])
data class GoalStepEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val goalId: String,
    /** JourneyPeriod.key */
    val periodKey: String,
    val orderIndex: Int = 0,
    val title: String,
    val detail: String = "",
    val status: MilestoneStatus = MilestoneStatus.UPCOMING,
    val taskId: String? = null,
    val doneAt: Long? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
