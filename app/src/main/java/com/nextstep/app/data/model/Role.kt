package com.nextstep.app.data.model

/** 앱 사용자 역할. 학생과 학부모는 서로 다른 화면을 봅니다. */
enum class Role(val label: String, val description: String) {
    STUDENT("학생", "내 시간표, 진도, 성적을 기록하고 예습·복습 제안을 받아요"),
    PARENT("학부모", "자녀의 학습 현황을 실시간으로 확인하고 일정·메모를 남겨요"),
    /** 선생님, 과외, 튜터, 멘토 등 학습을 지도하는 모든 사람. 한 학생에 여러 명이 연결될 수 있습니다. */
    MENTOR("멘토", "선생님·과외·튜터로서 담당 과목의 진도와 과제를 관리하고 피드백을 남겨요");

    companion object {
        fun from(value: String?): Role? = entries.firstOrNull { it.name == value }
        fun labelOf(value: String?): String = from(value)?.label ?: "알 수 없음"
    }
}
