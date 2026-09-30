package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.MilestoneStatus

/**
 * 여정 이정표의 저장 상태. 카탈로그 항목은 templateId 로 연결되어 상태·메모·날짜 변경만 저장하고,
 * 직접 추가한 항목은 templateId 가 null 이며 내용 전부를 가집니다.
 */
@Entity(tableName = "journey_items", indices = [Index("familyId"), Index("templateId")])
data class JourneyItemEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val templateId: String? = null,
    val title: String = "",
    val description: String = "",
    /** MilestoneCategory.name */
    val category: String = "",
    /** 마감일 (epoch day). 카탈로그 항목은 사용자가 바꿨을 때만 의미 있음. */
    val dueDate: Long = 0L,
    val leadMonths: Int = 1,
    val priority: Int = 2,
    val status: MilestoneStatus = MilestoneStatus.UPCOMING,
    val note: String = "",
    val doneAt: Long? = null,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
