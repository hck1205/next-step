package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * 레이더(방사형) 차트. values 는 0~1 로 정규화된 값.
 */
@Composable
fun RadarChart(axes: List<String>, values: List<Float>, color: Color, modifier: Modifier = Modifier, chartSize: Int = 220, secondary: List<Float>? = null, secondaryColor: Color = Color.Gray) {
    val measurer = rememberTextMeasurer()
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val labelStyle = TextStyle(fontSize = 11.sp, color = labelColor)
    Canvas(modifier.size(chartSize.dp)) {
        val n = axes.size
        if (n < 3) return@Canvas
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 28.dp.toPx()
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
