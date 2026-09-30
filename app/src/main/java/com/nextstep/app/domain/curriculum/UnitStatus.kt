package com.nextstep.app.domain.curriculum

enum class UnitStatus(val label: String) {
    NOT_REGISTERED("미등록"),
    REGISTERED("등록됨"),
    IN_CLASS("학교 진도 중"),
    DONE("끝냄"),
}
