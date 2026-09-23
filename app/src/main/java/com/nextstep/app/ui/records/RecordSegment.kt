package com.nextstep.app.ui.records

/** 기록 탭의 세그먼트. 첫 번째는 항상 균형입니다. */
enum class RecordSegment(val label: String, val route: String) {
    BALANCE("균형", "balance"),
    LEARNING("학습", "learning"),
    GRADES("성적", "grades"),
    PROGRESS("진도", "progress"),
    CALENDAR("일정", "calendar");

    companion object {
        fun from(value: String?): RecordSegment = entries.firstOrNull { it.route == value } ?: BALANCE
    }
}
