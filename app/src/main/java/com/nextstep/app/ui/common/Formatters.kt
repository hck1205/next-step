package com.nextstep.app.ui.common

import java.util.Locale

/** 화면 숫자 표기. 정수면 정수로, 아니면 소수 첫째 자리까지. */
fun Double.oneDecimal(): String = if (this == toLong().toDouble()) toLong().toString() else String.format(Locale.ROOT, "%.1f", this)

fun Float.asPercent(): String = "${(this * 100).toInt()}%"

/** 0~1 비율을 백분율 정수로. */
fun ratioPercent(part: Int, whole: Int): Int = if (whole <= 0) 0 else part * 100 / whole
