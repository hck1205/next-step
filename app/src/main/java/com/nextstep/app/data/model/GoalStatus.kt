package com.nextstep.app.data.model

/** 장기 목표의 상태. */
enum class GoalStatus(val label: String) {
    ACTIVE("진행 중"),
    DONE("달성"),
    ARCHIVED("보관");

    companion object {
        fun from(value: String?): GoalStatus = entries.firstOrNull { it.name == value } ?: ACTIVE
    }
}
