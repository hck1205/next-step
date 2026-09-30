package com.nextstep.app.domain.stats

/** 아이용 "나" 화면: 최근 4주 스티커판, 모은 스티커 수, 이번 주 끝낸 할 일 수. 숫자 비교 없이 모으는 재미만. */
data class StickerBoard(
    val days: List<StickerDay>,
    val stickers: Int,
    val doneThisWeek: Int,
)
