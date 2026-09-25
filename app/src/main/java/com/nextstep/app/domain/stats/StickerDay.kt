package com.nextstep.app.domain.stats

import java.time.LocalDate

/** 스티커판의 하루: 공부 기록이 있으면 별, 활동이 있으면 꽃. */
data class StickerDay(val date: LocalDate, val studied: Boolean, val didActivity: Boolean) {
    val hasSticker: Boolean get() = studied || didActivity
}
