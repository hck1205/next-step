package com.nextstep.app.ui.progress

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.data.model.TopicStatus

data class SubjectDetailUiState(
    val subject: SubjectEntity? = null,
    val topics: List<TopicEntity> = emptyList(),
    val subjects: List<SubjectEntity> = emptyList(),
) {
    val classIndex: Int get() = topics.filter { it.classCovered }.maxOfOrNull { it.orderIndex } ?: -1
    val previewQueue: List<TopicEntity> get() = topics.filter { !it.classCovered && it.status.order < TopicStatus.PREVIEWED.order }
    val reviewQueue: List<TopicEntity> get() = topics.filter { it.classCovered && it.status.order < TopicStatus.REVIEWED.order }
}
