package com.nextstep.app.ui.content

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.domain.content.ContentRecommendation

data class ContentUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val all: List<ContentEntity> = emptyList(),
    val filter: ContentFilter = ContentFilter(),
    val recommendations: List<ContentRecommendation> = emptyList(),
    val add: AddContentState = AddContentState(),
    /** 과목 칩 목록과 필터 결과. ViewModel 이 한 번 계산합니다. */
    val subjectKeys: List<String> = emptyList(),
    val filtered: List<ContentEntity> = emptyList(),
)
