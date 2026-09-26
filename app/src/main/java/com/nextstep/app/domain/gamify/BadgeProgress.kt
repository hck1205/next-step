package com.nextstep.app.domain.gamify

/** 배지 하나의 지금: 받았는지, 받기까지 얼마나 왔는지. */
data class BadgeProgress(val badge: Badge, val value: Int) {
    val earned: Boolean get() = value >= badge.target
    val ratio: Float get() = (value.toFloat() / badge.target).coerceIn(0f, 1f)
}
