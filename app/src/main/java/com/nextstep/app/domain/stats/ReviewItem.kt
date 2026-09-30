package com.nextstep.app.domain.stats

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.local.entity.TopicEntity

/** 복습 목록의 한 줄: 어느 과목의 어느 단원을, 왜. */
data class ReviewItem(val subject: SubjectEntity, val topic: TopicEntity, val reason: ReviewReason)
