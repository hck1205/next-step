package com.nextstep.app.domain.journey

/** 오늘 기준 타임라인 구간. */
enum class JourneyPhase(val label: String) {
    OVERDUE("지난 항목"),
    NOW("지금 준비할 것"),
    UPCOMING("다가오는 것"),
    DONE("완료"),
    SKIPPED("건너뜀"),
}
