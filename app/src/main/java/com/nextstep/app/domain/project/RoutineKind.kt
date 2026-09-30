package com.nextstep.app.domain.project

/** 루틴 한 줄의 종류. 화면 아이콘과 "무엇을 하는지" 묶음에 씁니다. */
enum class RoutineKind(val label: String) {
    LISTEN("듣기"),
    READ("읽기"),
    SPEAK("말하기"),
    WRITE("쓰기"),
    PLAY("놀이"),
    PRACTICE("연습"),
    REVIEW("복습"),
}
