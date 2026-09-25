package com.nextstep.app.domain.growth

/** 한 해 공부 종류의 갈래. 화면은 이 값으로 아이콘을 고릅니다. */
enum class StudyKindType(val label: String) {
    PLAY("놀이"),
    READ("읽기"),
    WRITE("쓰기"),
    PRACTICE("연습"),
    TEST_PREP("시험 준비"),
    PROJECT("수행·탐구"),
    CAREER("진로"),
    HABIT("습관"),
}
