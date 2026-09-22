package com.nextstep.app.data.model

/** 여정 이정표의 진행 상태. */
enum class MilestoneStatus(val label: String) {
    UPCOMING("예정"),
    IN_PROGRESS("진행 중"),
    DONE("완료"),
    SKIPPED("건너뜀");

    companion object {
        fun from(value: String?): MilestoneStatus = entries.firstOrNull { it.name == value } ?: UPCOMING
    }
}
