package com.nextstep.app.ui.content

import com.nextstep.app.data.local.entity.ContentEntity
import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.GradeLevel
import com.nextstep.app.domain.content.ContentRecommendation

data class ContentUiState(
    val subjects: List<SubjectEntity> = emptyList(),
    val all: List<ContentEntity> = emptyList(),
    val filter: ContentFilter = ContentFilter(),
    val recommendations: List<ContentRecommendation> = emptyList(),
    val add: AddContentState = AddContentState(),
) {
    val subjectKeys: List<String> get() = (subjects.map { it.name } + all.map { it.subjectKey }).filter { it.isNotBlank() }.distinct()
    val filtered: List<ContentEntity> get() = all.filter { c ->
        (filter.subjectKey == null || c.subjectKey == filter.subjectKey) &&
            (filter.type == null || c.contentType == filter.type) &&
            (filter.level == null || c.gradeLevel == filter.level || c.gradeLevel == GradeLevel.ALL) &&
            (!filter.hideWatched || !c.watched) &&
            (filter.query.isBlank() || listOf(c.title, c.channel, c.keywords, c.summary).any { it.contains(filter.query, ignoreCase = true) })
    }
}
