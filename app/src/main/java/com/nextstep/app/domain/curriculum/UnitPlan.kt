package com.nextstep.app.domain.curriculum

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.TopicEntity

data class UnitPlan(
    val unit: CurriculumUnit,
    val status: UnitStatus,
    val matchedTopic: TopicEntity?,
    /** 같은 단원을 등록한 다른 가족 수. 모르면 null. */
    val peerFamilies: Int?,
    /** 저장소에서 찾은 영상(최대 2개). */
    val videos: List<ContentEntity>,
    /** 저장소에 없을 때 열 유튜브 검색 주소. */
    val searchUrl: String,
    /** 지금 할 일 제안 한 줄. 없으면 null. */
    val suggestion: String?,
)
