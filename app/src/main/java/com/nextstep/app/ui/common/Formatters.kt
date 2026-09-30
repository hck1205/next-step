package com.nextstep.app.ui.common

import com.nextstep.app.domain.text.NumberText
import com.nextstep.app.domain.text.toPercent

/** 화면 숫자 표기. 정수면 정수로, 아니면 소수 첫째 자리까지. 문장 속 숫자와 같은 규칙(NumberText). */
fun Double.oneDecimal(): String = NumberText.compact(this)

fun Float.asPercent(): String = "${toPercent()}%"
