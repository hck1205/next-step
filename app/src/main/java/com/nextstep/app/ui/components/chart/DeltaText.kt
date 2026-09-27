package com.nextstep.app.ui.components.chart

import kotlin.math.abs

/** 지표 타일의 변화 글: 부호 + 크기 + 무엇과 비교했는지(예: "+25분 · 전 7일 대비", "−4점 · 직전 대비"). */
fun deltaText(change: Int, format: (Int) -> String, versus: String): String =
    "${if (change >= 0) "+" else "−"}${format(abs(change))} · $versus"
