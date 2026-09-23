package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

/** 막대 하나의 데이터. */
data class BarItem(val label: String, val value: Float, val color: Color, val goal: Float? = null)

/**
 * 세로 막대 차트. 값 라벨과 x축 라벨을 함께 그립니다. goal 이 있으면 얇은 목표선을 그립니다.
 */
@Composable
fun BarChart(items: List<BarItem>, modifier: Modifier = Modifier, valueFormatter: (Float) -> String = { it.toInt().toString() }, height: Int = 160) {
    val measurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)
    Canvas(modifier.fillMaxWidth().height(height.dp)) {
        if (items.isEmpty()) return@Canvas
        val maxV = max(items.maxOf { max(it.value, it.goal ?: 0f) }, 1f)
        val bottomPad = 18.dp.toPx()
        val topPad = 16.dp.toPx()
        val chartH = size.height - bottomPad - topPad
        val slot = size.width / items.size
        val barW = slot * 0.55f
        // grid lines
        for (i in 0..3) {
            val y = topPad + chartH * i / 3f
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
        items.forEachIndexed { i, item ->
            val x = slot * i + (slot - barW) / 2
            val h = chartH * (item.value / maxV)
            drawRoundRect(item.color, Offset(x, topPad + chartH - h), Size(barW, h), CornerRadius(6.dp.toPx()))
            item.goal?.takeIf { it > 0 }?.let { g ->
                val gy = topPad + chartH - chartH * (g / maxV)
                drawLine(item.color.copy(alpha = 0.6f), Offset(x - 4.dp.toPx(), gy), Offset(x + barW + 4.dp.toPx(), gy), strokeWidth = 2f, cap = StrokeCap.Round)
            }
            if (item.value > 0) {
                val v = measurer.measure(valueFormatter(item.value), labelStyle)
                drawText(v, topLeft = Offset(x + barW / 2 - v.size.width / 2, topPad + chartH - h - v.size.height - 2.dp.toPx()))
            }
            val l = measurer.measure(item.label, labelStyle)
            drawText(l, topLeft = Offset(x + barW / 2 - l.size.width / 2, size.height - bottomPad + 3.dp.toPx()))
        }
    }
}
