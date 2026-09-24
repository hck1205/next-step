package com.nextstep.app.domain.journey

enum class GoalArea(val label: String) {
    LANGUAGE("영어·외국어"),
    MATH("수학"),
    KOREAN("국어·읽기"),
    HABIT("습관·자기주도"),
    EXPERIENCE("경험·예체능"),
    HOBBY("취미"),
    CLUB("동아리·활동"),
    CAREER("진로·학업"),
    EXAM("시험"),
    PERFORMANCE("수행평가"),
    ADMISSION("입시"),
    CUSTOM("직접 만든 목표");

    companion object {
        fun from(value: String?): GoalArea = entries.firstOrNull { it.name == value } ?: CUSTOM
    }
}
