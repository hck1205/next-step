package com.nextstep.app.data.model

enum class EventType(val label: String) {
    CLASS("수업"),
    ACADEMY("학원"),
    STUDY("자습"),
    EXAM("시험"),
    OTHER("기타");

    companion object {
        fun from(value: String?): EventType = entries.firstOrNull { it.name == value } ?: OTHER
    }
}
