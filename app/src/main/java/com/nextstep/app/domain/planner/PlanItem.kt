package com.nextstep.app.domain.planner

import com.nextstep.app.data.local.entity.SubjectEntity
import com.nextstep.app.data.model.TaskType

/** 계획에 넣을 학습 항목 하나. 복습/예습 단원이나 로드맵 항목에서 만들어집니다. */
data class PlanItem(
    val subject: SubjectEntity?,
    val title: String,
    val taskType: TaskType,
    val topicId: String? = null,
    val roadmapId: String? = null,
)
