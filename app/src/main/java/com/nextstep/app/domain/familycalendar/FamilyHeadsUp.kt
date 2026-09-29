package com.nextstep.app.domain.familycalendar

/** 오늘 화면의 "가족 일정" 카드에 며칠 전부터 미리 띄울지([days]). 이름은 저장값이라 바꾸지 않습니다. */
enum class FamilyHeadsUp(val label: String, val days: Int) {
    SAME_DAY("당일", 0),
    DAY_BEFORE("전날부터", 1),
    THREE_DAYS("3일 전부터", 3),
    WEEK("일주일 전부터", 7);

    companion object {
        fun from(value: String?): FamilyHeadsUp = entries.firstOrNull { it.name == value } ?: DAY_BEFORE
    }
}
