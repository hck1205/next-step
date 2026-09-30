package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * 한 계열 세로 막대(요일별 공부 시간·주별 달성률 등). 막대는 24dp 이하·위 끝만 4dp 둥글게, 눈금은 가는 선 세 줄.
 * 값 글자는 고른 막대 하나에만(처음엔 맨 끝 = 지금), 막대를 누르면 그 막대 값이 보입니다. 목표선은 가는 실선 + 아래 범례.
 */
@Composable
fun ColumnChart(
    values: List<Int>,
    labels: List<String>,
    description: String,
    modifier: Modifier = Modifier,
    goal: Int? = null,
    goalLabel: String? = null,
    max: Int? = null,
    format: (Int) -> String = { it.toString() },
    tickFormat: (Int) -> String = format,
    height: Dp = ChartHeights.column(compact = false),
) {
    if (values.isEmpty()) return
    val palette = ChartPalette.current()
    val measurer = rememberTextMeasurer()
    var selected by remember(values) { mutableIntStateOf(values.lastIndex) }
    val top = max ?: niceMax(maxOf(values.max(), goal ?: 0))
    Column(modifier) {
        Canvas(
            Modifier.fillMaxWidth().height(height)
                .semantics { contentDescription = description }
                .pointerInput(values) {
                    detectTapGestures { p ->
                        val band = (size.width - AXIS_DP.dp.toPx()) / values.size
                        selected = ((p.x - AXIS_DP.dp.toPx()) / band).toInt().coerceIn(0, values.lastIndex)
                    }
                },
        ) {
            val base = size.height - BOTTOM_DP.dp.toPx()
            val y = { v: Int -> base - (min(v, top).toFloat() / top) * (base - TOP_DP.dp.toPx()) }
            columnGrid(measurer, palette, listOf(0, top / 2, top), y, tickFormat)
            values.forEachIndexed { i, v -> columnBar(i, values.size, v, i == selected, palette, y(v), base) }
            goal?.let { g -> drawLine(palette.ink.copy(alpha = GOAL_ALPHA), Offset(AXIS_DP.dp.toPx(), y(g)), Offset(size.width, y(g)), strokeWidth = 1.25.dp.toPx()) }
            columnLabels(measurer, palette, labels, selected, base)
            values.getOrNull(selected)?.takeIf { it > 0 }?.let { v -> columnValue(measurer, palette, format(v), selected, values.size, y(v)) }
        }
        if (goal != null && goalLabel != null) ChartLegend(listOf(LegendKey(goalLabel, palette.ink.copy(alpha = GOAL_ALPHA), line = true)))
    }
}

private fun DrawScope.columnGrid(measurer: TextMeasurer, palette: ChartPalette, ticks: List<Int>, y: (Int) -> Float, tickFormat: (Int) -> String) {
    val left = AXIS_DP.dp.toPx()
    val style = TextStyle(fontSize = 10.sp, color = palette.ink.copy(alpha = MUTED_ALPHA))
    ticks.forEach { t ->
        drawLine(palette.grid, Offset(left, y(t)), Offset(size.width, y(t)), strokeWidth = 1.dp.toPx())
        val text = measurer.measure(tickFormat(t), style)
        drawText(text, topLeft = Offset(left - text.size.width - 4.dp.toPx(), y(t) - text.size.height / 2))
    }
}

private fun DrawScope.columnBar(i: Int, count: Int, v: Int, selected: Boolean, palette: ChartPalette, top: Float, base: Float) {
    val left = AXIS_DP.dp.toPx()
    val band = (size.width - left) / count
    val w = min(MAX_BAR_DP.dp.toPx(), band * BAR_SHARE)
    val x = left + band * i + (band - w) / 2
    if (v <= 0) {
        drawRect(palette.muted, Offset(x, base - 2.dp.toPx()), Size(w, 2.dp.toPx()))
        return
    }
    val r = CornerRadius(4.dp.toPx())
    drawPath(Path().apply { addRoundRect(RoundRect(x, top, x + w, base, r, r, CornerRadius.Zero, CornerRadius.Zero)) }, if (selected) palette.accent else palette.heat[2])
}

private fun DrawScope.columnLabels(measurer: TextMeasurer, palette: ChartPalette, labels: List<String>, selected: Int, base: Float) {
    val left = AXIS_DP.dp.toPx()
    val band = (size.width - left) / labels.size
    labels.forEachIndexed { i, l ->
        val on = i == selected
        val style = TextStyle(fontSize = 10.sp, color = palette.ink.copy(alpha = if (on) 1f else MUTED_ALPHA), fontWeight = if (on) FontWeight.Bold else null)
        val text = measurer.measure(l, style)
        drawText(text, topLeft = Offset(left + band * i + band / 2 - text.size.width / 2, base + 3.dp.toPx()))
    }
}

private fun DrawScope.columnValue(measurer: TextMeasurer, palette: ChartPalette, label: String, i: Int, count: Int, top: Float) {
    val left = AXIS_DP.dp.toPx()
    val band = (size.width - left) / count
    val text = measurer.measure(label, TextStyle(fontSize = 11.sp, color = palette.ink, fontWeight = FontWeight.Bold))
    drawText(text, topLeft = Offset(left + band * i + band / 2 - text.size.width / 2, top - text.size.height - 2.dp.toPx()))
}

/** 눈금 위 끝: 1·2·2.5·5·10 × 10ⁿ 중 값보다 큰 가장 작은 것. */
internal fun niceMax(v: Int): Int {
    if (v <= 0) return NICE_FLOOR
    var p = 1
    while (p * NICE_BASE <= v) p *= NICE_BASE
    return listOf(p, p * 2, p * 5 / 2, p * 5, p * NICE_BASE).first { it >= v }
}

private const val AXIS_DP = 34
private const val TOP_DP = 16
private const val BOTTOM_DP = 18
private const val MAX_BAR_DP = 24
private const val BAR_SHARE = 0.58f
private const val MUTED_ALPHA = 0.6f
private const val GOAL_ALPHA = 0.55f
private const val NICE_FLOOR = 10
private const val NICE_BASE = 10
