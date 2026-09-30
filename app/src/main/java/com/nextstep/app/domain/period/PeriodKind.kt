package com.nextstep.app.domain.period

/** 리포트의 길이: 한 달 또는 한 학기(1학기 3~8월, 2학기 9~2월). */
enum class PeriodKind(val label: String, val previousLabel: String) {
    MONTH("월간", "지난달"),
    TERM("학기", "지난 학기"),
}
