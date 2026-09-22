package com.nextstep.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
