package com.nextstep.app.domain.familycalendar

/** 가족 일정의 종류. 달력 칸의 색·아이콘과 새 일정의 기본값(FamilyEventDraft.withKind)을 정합니다. 이름은 저장값이라 바꾸지 않습니다. */
enum class FamilyEventKind(val label: String) {
    FAMILY("가족"),
    OUTING("나들이·여행"),
    CELEBRATION("생일·기념일"),
    HOSPITAL("병원"),
    SCHOOL("학교"),
    LESSON("학원·수업"),
    WORK("일·출장"),
    PROMISE("약속"),
    HOME("집안일");

    companion object {
        fun from(value: String?): FamilyEventKind = entries.firstOrNull { it.name == value } ?: FAMILY
    }
}
