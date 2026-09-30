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
    /** 날짜가 정해진 목표의 단계 마감(epochDay). 학기 단위 단계는 null. */
    val dueDate: Long? = null,
    val doneAt: Long? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
