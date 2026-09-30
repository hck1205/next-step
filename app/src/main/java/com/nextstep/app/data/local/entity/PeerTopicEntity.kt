package com.nextstep.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 다른 가족들이 같은 구간(학기)에 등록한 단원의 익명 통계. 서버 집계 문서를 읽기 전용으로 받습니다.
 * "이 시기 다른 가족 12곳이 '정수와 유리수'를 배웠어요" 같은 참고선이 됩니다. familyId 는 공용이라 비어 있습니다.
 */
@Entity(tableName = "peer_topics", indices = [Index("periodKey")])
data class PeerTopicEntity(
    @PrimaryKey override val id: String = newId(),
    override val familyId: String = "",
    /** JourneyPeriod.key (예: g7s1). */
    val periodKey: String,
    val subject: String,
    val title: String,
    /** 이 단원을 등록한 가족 수. */
    val families: Int = 0,
    /** 학급 진도 완료 비율(0~1). 모르면 0. */
    val coveredRatio: Double = 0.0,
    override val updatedAt: Long = 0L,
    override val deleted: Boolean = false,
    override val dirty: Boolean = false,
) : Syncable
