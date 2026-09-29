package com.nextstep.app.domain.cheer

/** 응원의 종류: 그림 하나와 한마디. 이름은 저장값이라 바꾸지 않습니다. 점수·크기 차이는 없습니다(어느 것이든 같은 응원). */
enum class CheerKind(val emoji: String, val word: String) {
    CLAP("👏", "짝짝짝"),
    HEART("❤️", "사랑해"),
    STAR("⭐", "최고야"),
    MUSCLE("💪", "멋지다");

    companion object {
        fun from(value: String?): CheerKind = entries.firstOrNull { it.name == value } ?: CLAP
    }
}
