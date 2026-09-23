package com.nextstep.app.domain.text

import java.util.Locale
import kotlin.math.floor

/** 문장 속 숫자 표기. 정수면 소수점 없이, 아니면 소수 첫째 자리까지. */
object NumberText {
    fun compact(value: Double): String = if (value == floor(value)) value.toLong().toString() else String.format(Locale.ROOT, "%.1f", value)
}

fun Double.compact(): String = NumberText.compact(this)
