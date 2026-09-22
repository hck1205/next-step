package com.nextstep.app.ui.grades.components

import java.util.Locale

/** 정수면 소수점 없이, 아니면 소수 첫째 자리까지. */
internal fun Double.trim(): String = if (this == Math.floor(this)) toInt().toString() else String.format(Locale.ROOT, "%.1f", this)
