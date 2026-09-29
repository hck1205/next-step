package com.nextstep.app.domain.familycalendar

/** 가족 일정의 반복. 이름은 저장값이라 바꾸지 않습니다. */
enum class FamilyRepeat(val label: String) {
    NONE("안 함"),
    WEEKLY("매주"),
    MONTHLY("매달"),
    YEARLY("매년");

    companion object {
        fun from(value: String?): FamilyRepeat = entries.firstOrNull { it.name == value } ?: NONE
    }
}
