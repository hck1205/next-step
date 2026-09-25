package com.nextstep.app.domain.year

/** "올해" 탭의 분류(탭). 순서가 곧 탭 순서이고, 그 해에 할 일이 있는 분류만 탭이 됩니다. */
enum class YearArea(val label: String) {
    TALK("말·듣기"),
    PLAY("놀이·표현"),
    KOREAN("국어"),
    MATH("수학"),
    ENGLISH("영어"),
    SOCIETY("사회·역사"),
    SCIENCE("과학"),
    READING("독서"),
    ARTS("예체능"),
    EXAM("시험"),
    RECORD("수행·학생부"),
    CAREER("진로·입시"),
    LIFE("생활 습관"),
    BODY("몸·건강"),
    MIND("마음 건강"),
    GUIDE("상담·코칭"),
    ADMIN("지원·서류"),
}
