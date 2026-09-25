package com.nextstep.app.domain.hub

/**
 * 기록 탭(학생은 "나")을 나누는 관심사. 기능은 반드시 한 관심사에만 속하고, 관심사 안에서 섹션으로 나뉩니다.
 * 탭 위 한 줄(관심사)과 그 아래 한 줄(섹션)의 두 단 구조라 기능이 늘어도 하단 탭은 늘지 않습니다.
 */
enum class Concern(val label: String, val question: String) {
    OVERVIEW("한눈에", "전체적으로 어때?"),
    STUDY("공부", "무엇을 얼마나 배우고 있지?"),
    EXAMS("시험·성적", "시험 준비와 결과는?"),
    GROWTH("성장", "몸은 잘 자라고 있지?"),
    DISCOVER("활동·재능", "무엇을 좋아하고 잘하지?"),
}
