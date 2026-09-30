package com.nextstep.app.ui.components.chart

import androidx.compose.ui.graphics.Color

/** 범례 한 칸: 색 표시 + 이름. [line] 이면 네모 대신 가는 선(목표선·기준선). */
data class LegendKey(val label: String, val color: Color, val line: Boolean = false)
