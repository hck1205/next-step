package com.nextstep.app.data.model

enum class TaskType(val label: String) {
    PREVIEW("예습"),
    REVIEW("복습"),
    HOMEWORK("숙제"),
    EXAM_PREP("시험 준비"),
    OTHER("기타");

    companion object {
        fun from(value: String?): TaskType = entries.firstOrNull { it.name == value } ?: OTHER
    }
}
