package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.nextstep.app.data.model.AptitudeDomain

/**
 * 소질 관찰 메모. "노래를 듣고 바로 따라 부른다"처럼 어른이 본 장면을 영역과 강도(1~3)로 남깁니다.
 * 활동 기록과 함께 AptitudeEngine 의 입력이 됩니다.
 */
@Entity(tableName = "observations", indices = [Index("familyId")])
data class ObservationEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String,
    val domain: AptitudeDomain = AptitudeDomain.LOGIC,
    val text: String,
    /** 1 = 그런 것 같다, 2 = 분명히 보인다, 3 = 남들이 먼저 알아본다. */
    val strength: Int = 2,
    val date: Long,
    val authorRole: String = "",
    val authorName: String = "",
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deleted: Boolean = false,
    override val dirty: Boolean = true,
) : Syncable
