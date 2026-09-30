package com.nextstep.app.data.repository

import com.nextstep.app.domain.content.ContentClassification

/** 링크 등록 화면에 채워 넣을 초안. 메타데이터와 자동 분류 결과를 담습니다. */
data class ContentDraft(
    val url: String,
    val videoId: String,
    val title: String,
    val channel: String,
    val thumbnailUrl: String,
    val classification: ContentClassification,
    val metadataFetched: Boolean,
)
