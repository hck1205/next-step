package com.nextstep.app.data.model

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
