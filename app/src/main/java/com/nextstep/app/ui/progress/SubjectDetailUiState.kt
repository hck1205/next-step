package com.nextstep.app.ui.progress

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity

/** 과목 상세. 진도 위치와 예습·복습 목록은 ViewModel 이 StudyQueues 로 한 번 계산합니다. */
data class SubjectDetailUiState(
    val subject: SubjectEntity? = null,
    val topics: List<TopicEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
    val classIndex: Int = -1,
    val previewQueue: List<TopicEntity> = emptyList(),
    val reviewQueue: List<TopicEntity> = emptyList(),
)
