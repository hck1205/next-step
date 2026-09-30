package com.nextstep.app.data.model

/** 여정 이정표의 진행 상태. */
enum class MilestoneStatus(val label: String) {
    UPCOMING("예정"),
    IN_PROGRESS("진행 중"),
    DONE("완료"),
    SKIPPED("건너뜀");

    /** 끝난 상태(완료했거나 건너뜀). 진행률·다음 단계 찾기에서 빠집니다. */
    val isClosed: Boolean get() = this == DONE || this == SKIPPED

    companion object {
        fun from(value: String?): MilestoneStatus = entries.firstOrNull { it.name == value } ?: UPCOMING
    }
}
