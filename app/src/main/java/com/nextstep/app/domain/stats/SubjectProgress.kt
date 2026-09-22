package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity

/** 과목별 진도 요약. */
data class SubjectProgress(
    val subject: SubjectEntity,
    val total: Int,
    val classCovered: Int,
    val reviewed: Int,
    val previewed: Int,
    val previewQueue: List<TopicEntity>,
    val reviewQueue: List<TopicEntity>,
) {
    val classRatio: Float get() = if (total == 0) 0f else classCovered.toFloat() / total
    val myRatio: Float get() = if (total == 0) 0f else reviewed.toFloat() / total
}
