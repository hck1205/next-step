package com.nextstep.app.ui.content

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.model.ContentType
import com.nextstep.app.data.model.GradeLevel

data class ContentFilter(
    val query: String = "",
    val subjectKey: String? = null,
    val type: ContentType? = null,
    val level: GradeLevel? = null,
    val hideWatched: Boolean = false,
) {
    fun matches(c: ContentEntity): Boolean =
        (subjectKey == null || c.subjectKey == subjectKey) &&
            (type == null || c.contentType == type) &&
            (level == null || c.gradeLevel == level || c.gradeLevel == GradeLevel.ALL) &&
            (!hideWatched || !c.watched) &&
            (query.isBlank() || listOf(c.title, c.channel, c.keywords, c.summary).any { it.contains(query, ignoreCase = true) })
}
