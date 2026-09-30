package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity
import com.nextstep.app.domain.text.ratioOf

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
    val classRatio: Float get() = ratioOf(classCovered, total)
    val myRatio: Float get() = ratioOf(reviewed, total)
}
