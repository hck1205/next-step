package com.nextstep.app.domain.taskboard

import com.nextstep.app.data.model.TaskType
import java.time.LocalDate

/** 시스템이 제안한 할 일 한 줄: 어느 과목의 어느 단원을, 무엇으로, 언제까지, 왜. "할 일로"를 누르면 그대로 할 일이 됩니다. */
data class TaskSuggestion(
    val subjectId: String?,
    val topicId: String?,
    val title: String,
    val type: TaskType,
    val source: SuggestionSource,
    val due: LocalDate,
) {
    val key: String get() = "${source.name}:${subjectId.orEmpty()}:${topicId ?: title}"
}
