package com.nextstep.app.domain.planner

import com.nextstep.app.data.local.entity.EventEntity
import com.nextstep.app.data.local.entity.TaskEntity

data class StudyPlan(val events: List<EventEntity>, val tasks: List<TaskEntity>) {
    val isEmpty: Boolean get() = events.isEmpty()
}
