package com.nextstep.app.ui.content

import com.nextstep.app.data.repository.ContentDraft

/** 링크 등록 다이얼로그 상태. */
data class AddContentState(
    val url: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val draft: ContentDraft? = null,
)
