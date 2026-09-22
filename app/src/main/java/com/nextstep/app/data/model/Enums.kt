package com.nextstep.app.data.model

/** 앱 사용자 역할. 학생과 학부모는 서로 다른 화면을 봅니다. */
enum class Role(val label: String) {
    STUDENT("학생"),
    PARENT("학부모");

    companion object {
        fun from(value: String?): Role? = entries.firstOrNull { it.name == value }
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
