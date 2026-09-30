package com.nextstep.app.domain.text

/** 0~1 비율. 분모가 0 이하면 0. 진행 막대·달성률에 씁니다(화면의 ratio 도 이것). */
fun ratioOf(part: Int, whole: Int): Float = if (whole <= 0) 0f else part.toFloat() / whole

/** 0~1 비율. 분모가 0 이하면 null(아직 셀 것이 없음). */
fun ratioOrNull(part: Int, whole: Int): Float? = if (whole <= 0) null else part.toFloat() / whole

/** 정수 백분율(버림). 분모가 0 이하면 0. */
fun percentOf(part: Int, whole: Int): Int = if (whole <= 0) 0 else part * PERCENT / whole

/** 0~1 비율 → 정수 백분율(버림). */
fun Float.toPercent(): Int = (this * PERCENT).toInt()

private const val PERCENT = 100
