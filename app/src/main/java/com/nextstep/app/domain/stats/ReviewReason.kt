package com.nextstep.app.domain.stats

/** 복습 목록에 오른 이유. 순서가 곧 급한 순서입니다. */
enum class ReviewReason(val label: String) {
    LOW_CONFIDENCE("이해도가 낮아요"),
    SCORE_DROP("최근 점수가 내려간 과목"),
    AFTER_CLASS("수업 뒤 아직 복습 전"),
    NEXT_CLASS("다음 수업 예습"),
}
