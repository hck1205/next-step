package com.nextstep.app.domain.taskboard

/** 시스템이 할 일을 제안한 까닭. 순서가 곧 급한 순서입니다. */
enum class SuggestionSource(val label: String) {
    EXAM("다가오는 시험 대비"),
    LOW_CONFIDENCE("이해도가 낮아요"),
    SCORE_DROP("최근 점수가 내려간 과목"),
    ROADMAP("멘토 로드맵"),
    AFTER_CLASS("수업 뒤 아직 복습 전"),
    NEXT_CLASS("다음 수업 예습"),
}
