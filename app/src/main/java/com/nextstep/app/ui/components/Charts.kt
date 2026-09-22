package com.nextstep.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

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

data class LineSeries(val name: String, val color: Color, val points: List<Float?>)

/**
 * 여러 계열을 한 번에 그리는 꺾은선 차트. 각 계열은 같은 길이의 points 를 가지며 null 은 결측입니다.
 */
@Composable
fun LineChart(series: List<LineSeries>, xLabels: List<String>, modifier: Modifier = Modifier, yMax: Float = 100f, yMin: Float = 0f, height: Int = 180) {
    val measurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(height.dp)) {
            val leftPad = 28.dp.toPx()
            val bottomPad = 18.dp.toPx()
            val topPad = 8.dp.toPx()
            val chartW = size.width - leftPad
            val chartH = size.height - bottomPad - topPad
            val n = xLabels.size
            fun xAt(i: Int) = if (n <= 1) leftPad + chartW / 2 else leftPad + chartW * i / (n - 1)
            fun yAt(v: Float) = topPad + chartH * (1 - ((v - yMin) / (yMax - yMin)).coerceIn(0f, 1f))
            for (i in 0..4) {
                val v = yMin + (yMax - yMin) * i / 4
                val y = yAt(v)
                drawLine(gridColor, Offset(leftPad, y), Offset(size.width, y), strokeWidth = 1f)
                val t = measurer.measure(v.toInt().toString(), labelStyle)
                drawText(t, topLeft = Offset(leftPad - t.size.width - 4.dp.toPx(), y - t.size.height / 2))
            }
            xLabels.forEachIndexed { i, label ->
                if (n > 8 && i % 2 == 1) return@forEachIndexed
                val t = measurer.measure(label, labelStyle)
                drawText(t, topLeft = Offset(xAt(i) - t.size.width / 2, size.height - bottomPad + 3.dp.toPx()))
            }
            series.forEach { s ->
                val path = Path()
                var started = false
                s.points.forEachIndexed { i, p ->
                    if (p == null) { started = false; return@forEachIndexed }
                    val o = Offset(xAt(i), yAt(p))
                    if (!started) { path.moveTo(o.x, o.y); started = true } else path.lineTo(o.x, o.y)
                }
                drawPath(path, s.color, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                s.points.forEachIndexed { i, p ->
                    if (p != null) drawCircle(s.color, radius = 4.dp.toPx(), center = Offset(xAt(i), yAt(p)))
                }
            }
        }
        if (series.size > 1) {
            Spacer(Modifier.height(4.dp))
            Legend(series.map { it.name to it.color })
        }
    }
}

/**
 * 레이더(방사형) 차트. values 는 0~1 로 정규화된 값.
 */
@Composable
fun RadarChart(axes: List<String>, values: List<Float>, color: Color, modifier: Modifier = Modifier, size: Int = 220, secondary: List<Float>? = null, secondaryColor: Color = Color.Gray) {
    val measurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = TextStyle(fontSize = 11.sp, color = labelColor)
    Canvas(modifier.size(size.dp)) {
        val n = axes.size
        if (n < 3) return@Canvas
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val radius = this.size.minDimension / 2 - 28.dp.toPx()
        fun point(i: Int, r: Float): Offset {
            val angle = -Math.PI / 2 + 2 * Math.PI * i / n
            return Offset(center.x + (r * cos(angle)).toFloat(), center.y + (r * sin(angle)).toFloat())
        }
        for (ring in 1..4) {
            val r = radius * ring / 4
            val p = Path()
            for (i in 0 until n) { val o = point(i, r); if (i == 0) p.moveTo(o.x, o.y) else p.lineTo(o.x, o.y) }
            p.close()
            drawPath(p, gridColor, style = Stroke(1f))
        }
        for (i in 0 until n) drawLine(gridColor, center, point(i, radius), 1f)
        fun drawSeries(vals: List<Float>, c: Color) {
            val p = Path()
            for (i in 0 until n) { val o = point(i, radius * vals.getOrElse(i) { 0f }.coerceIn(0f, 1f)); if (i == 0) p.moveTo(o.x, o.y) else p.lineTo(o.x, o.y) }
            p.close()
            drawPath(p, c.copy(alpha = 0.25f))
            drawPath(p, c, style = Stroke(2.dp.toPx()))
            for (i in 0 until n) drawCircle(c, 3.dp.toPx(), point(i, radius * vals.getOrElse(i) { 0f }.coerceIn(0f, 1f)))
        }
        secondary?.let { drawSeries(it, secondaryColor) }
        drawSeries(values, color)
        for (i in 0 until n) {
            val o = point(i, radius + 16.dp.toPx())
            val t = measurer.measure(axes[i], labelStyle)
            drawText(t, topLeft = Offset(o.x - t.size.width / 2, o.y - t.size.height / 2))
        }
    }
}

data class Slice(val label: String, val value: Float, val color: Color)

/** 도넛 차트 + 범례. */
@Composable
fun DonutChart(slices: List<Slice>, modifier: Modifier = Modifier, centerText: String? = null, size: Int = 140) {
    val measurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSurface
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(size.dp)) {
            val stroke = 18.dp.toPx()
            val rect = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            if (total <= 0f) {
                drawArc(emptyColor, 0f, 360f, false, topLeft, rect, style = Stroke(stroke))
            } else {
                var start = -90f
                slices.forEach { s ->
                    val sweep = 360f * s.value / total
                    drawArc(s.color, start, sweep - 2f, false, topLeft, rect, style = Stroke(stroke, cap = StrokeCap.Butt))
                    start += sweep
                }
            }
            centerText?.let {
                val t = measurer.measure(it, TextStyle(fontSize = 14.sp, color = textColor))
                drawText(t, topLeft = Offset(this.size.width / 2 - t.size.width / 2, this.size.height / 2 - t.size.height / 2))
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            slices.filter { it.value > 0 }.sortedByDescending { it.value }.take(6).forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                    ColorDot(s.color, 8)
                    Spacer(Modifier.width(6.dp))
                    Text(s.label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text("${if (total > 0) (s.value * 100 / total).toInt() else 0}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** 0~23시 학습 분포 히트 스트립. */
@Composable
fun HourHeatStrip(minutesByHour: IntArray, color: Color, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val base = MaterialTheme.colorScheme.surfaceVariant
    val labelStyle = TextStyle(fontSize = 9.sp, color = labelColor)
    Canvas(modifier.fillMaxWidth().height(44.dp)) {
        val maxV = max(minutesByHour.maxOrNull() ?: 0, 1)
        val gap = 2.dp.toPx()
        val cell = (size.width - gap * 23) / 24
        val h = 24.dp.toPx()
        for (i in 0 until 24) {
            val x = i * (cell + gap)
            val alpha = minutesByHour[i].toFloat() / maxV
            drawRoundRect(base, Offset(x, 0f), Size(cell, h), CornerRadius(3.dp.toPx()))
            if (alpha > 0f) drawRoundRect(color.copy(alpha = 0.2f + 0.8f * alpha), Offset(x, 0f), Size(cell, h), CornerRadius(3.dp.toPx()))
            if (i % 6 == 0) {
                val t = measurer.measure("${i}시", labelStyle)
                drawText(t, topLeft = Offset(x, h + 3.dp.toPx()))
            }
        }
    }
}
