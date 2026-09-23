package com.nextstep.app.ui.common

import com.nextstep.app.domain.text.NumberText

/** 화면 숫자 표기. 정수면 정수로, 아니면 소수 첫째 자리까지. 문장 속 숫자와 같은 규칙(NumberText). */
fun Double.oneDecimal(): String = NumberText.compact(this)

fun Float.asPercent(): String = "${(this * 100).toInt()}%"

/** 0~1 비율. 분모가 0 이하면 0. 진행 막대에 바로 넣습니다. */
fun ratio(part: Int, whole: Int): Float = if (whole <= 0) 0f else part.toFloat() / whole

/** 0~1 비율을 백분율 정수로. */
fun ratioPercent(part: Int, whole: Int): Int = if (whole <= 0) 0 else part * 100 / whole
