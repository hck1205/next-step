package com.nextstep.app.domain.report

/** 수업 리포트의 길이. 이름은 보낸 기록에 저장하는 값이라 바꾸지 않습니다. */
enum class ReportKind(val label: String) {
    WEEK("주간"),
    MONTH("월간");

    companion object {
        fun from(value: String?): ReportKind = entries.firstOrNull { it.name == value } ?: WEEK
    }
}
