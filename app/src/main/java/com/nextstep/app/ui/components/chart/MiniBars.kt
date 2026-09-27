package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min

/** 작은 막대 줄(지표 타일·기록 탭 타일): 지난 값은 옅은 색, 마지막(지금) 막대만 진한 색. 0 은 바닥에 얇은 회색. */
@Composable
fun MiniBars(values: List<Int>, modifier: Modifier = Modifier, height: Dp = 26.dp) {
    if (values.isEmpty()) return
    val palette = ChartPalette.current()
    Canvas(modifier.fillMaxWidth().height(height)) {
        val gap = 3.dp.toPx()
        val w = min(MAX_BAR_DP.dp.toPx(), (size.width - gap * (values.size - 1)) / values.size)
        val top = values.max().coerceAtLeast(1)
        values.forEachIndexed { i, v ->
            val h = if (v > 0) maxOf(3.dp.toPx(), v.toFloat() / top * size.height) else 2.dp.toPx()
            val color = when {
                i == values.lastIndex && v > 0 -> palette.accent
                v > 0 -> palette.heat[2]
                else -> palette.muted
            }
            drawRoundRect(color, Offset(i * (w + gap), size.height - h), Size(w, h), CornerRadius(2.dp.toPx()))
        }
    }
}

private const val MAX_BAR_DP = 14
