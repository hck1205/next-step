package com.nextstep.app.domain.selfdirection

/** 스스로 공부하는 한 바퀴(자기조절학습의 계획 → 실행 → 점검 → 돌아보기). 단계마다 누가 맡는지가 달라집니다. */
enum class LoopStep(val label: String, val question: String) {
    PLAN("계획", "이번 주에 무엇을 할까?"),
    DO("실행", "언제, 얼마나 할까?"),
    CHECK("점검", "계획대로 되고 있나?"),
    REFLECT("돌아보기", "잘된 것과 바꿀 것은?"),
}
