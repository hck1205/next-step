package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.ActivityType

/**
 * 실제로 한 활동 하나(취미·동아리·현장학습·체험·봉사·대회·여행). 날짜로 타임라인 구간에 놓입니다.
 * 취미·동아리처럼 이어지는 활동은 [endDate] 를 비워 두고 "진행 중"으로 봅니다.
 */
@Entity(tableName = "activities", indices = [Index("familyId")])
data class ActivityEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val type: ActivityType = ActivityType.OTHER,
    val title: String,
    /** 시작일 또는 당일 (epoch day). */
    val date: Long,
    /** 이어지는 활동의 종료일. null 이면 하루짜리이거나 진행 중. */
    val endDate: Long? = null,
    val place: String = "",
    /** 아이의 소감·배운 점. 포트폴리오 문장이 됩니다. */
    val note: String = "",
    /** 1~5. 0 이면 미입력. */
    val rating: Int = 0,
    val createdByRole: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable {
    val isOngoing: Boolean get() = endDate == null && type in ONGOING_TYPES

    private companion object {
        val ONGOING_TYPES = setOf(ActivityType.HOBBY, ActivityType.CLUB)
    }
}
