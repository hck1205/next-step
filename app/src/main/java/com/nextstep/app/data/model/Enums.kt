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

/** 단원(Topic)의 학습 상태. 예습/복습 목록은 이 상태와 학급 진도를 조합해 계산합니다. */
enum class TopicStatus(val label: String, val order: Int) {
    NOT_STARTED("시작 전", 0),
    PREVIEWED("예습 완료", 1),
    IN_CLASS("수업 진행", 2),
    REVIEWED("복습 완료", 3),
    MASTERED("완전 학습", 4);

    companion object {
        fun from(value: String?): TopicStatus = entries.firstOrNull { it.name == value } ?: NOT_STARTED
    }
}

enum class TaskType(val label: String) {
    PREVIEW("예습"),
    REVIEW("복습"),
    HOMEWORK("숙제"),
    EXAM_PREP("시험 준비"),
    OTHER("기타");

    companion object {
        fun from(value: String?): TaskType = entries.firstOrNull { it.name == value } ?: OTHER
    }
}

enum class EventType(val label: String) {
    CLASS("수업"),
    ACADEMY("학원"),
    STUDY("자습"),
    EXAM("시험"),
    OTHER("기타");

    companion object {
        fun from(value: String?): EventType = entries.firstOrNull { it.name == value } ?: OTHER
    }
}

enum class ExamType(val label: String) {
    MIDTERM("중간고사"),
    FINAL("기말고사"),
    QUIZ("쪽지시험"),
    MOCK("모의고사"),
    ASSIGNMENT("수행평가");

    companion object {
        fun from(value: String?): ExamType = entries.firstOrNull { it.name == value } ?: QUIZ
    }
}

enum class SyncStatus { LOCAL_ONLY, CONNECTING, SYNCED, ERROR }
