package com.nextstep.app.domain.notice

/** 알림의 때. 하루 한 번 아침([MORNING])과, 일요일 저녁 주말 이야기([WEEKEND]). 이름은 작업 예약에 쓰는 값이라 바꾸지 않습니다. */
enum class NoticeKind {
    MORNING,
    WEEKEND;

    companion object {
        fun from(value: String?): NoticeKind = entries.firstOrNull { it.name == value } ?: MORNING
    }
}
