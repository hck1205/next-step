package com.nextstep.app.ui.content

import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel

data class ContentFilter(
    val query: String = "",
    val subjectKey: String? = null,
    val type: ContentType? = null,
    val level: GradeLevel? = null,
    val hideWatched: Boolean = false,
)
