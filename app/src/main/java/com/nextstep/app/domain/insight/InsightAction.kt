package com.nextstep.app.domain.insight

import com.nextstep.app.data.model.TaskType

/** 인사이트에서 바로 만들 수 있는 실행 항목. */
sealed class InsightAction {
    data class CreateTask(val title: String, val subjectId: String?, val topicId: String?, val type: TaskType) : InsightAction()
}
