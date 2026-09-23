package com.nextstep.app.data.model

/** 활동 기록의 종류. 학교 밖 경험을 시기별로 남겨 포트폴리오·생기부·자유학기 자료가 됩니다. */
enum class ActivityType(val label: String) {
    HOBBY("취미"),
    CLUB("동아리"),
    FIELD_TRIP("현장학습"),
    EXPERIENCE("체험"),
    VOLUNTEER("봉사"),
    COMPETITION("대회·발표"),
    TRAVEL("여행"),
    OTHER("기타");

    companion object {
        fun from(value: String?): ActivityType = entries.firstOrNull { it.name == value } ?: OTHER
    }
}
