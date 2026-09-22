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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Slice(val label: String, val value: Float, val color: Color)

/** 도넛 차트 + 범례. */
@Composable
fun DonutChart(slices: List<Slice>, modifier: Modifier = Modifier, centerText: String? = null, chartSize: Int = 140) {
    val measurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSurface
    val emptyColor = MaterialTheme.colorScheme.surfaceVariant
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.size(chartSize.dp)) {
            val stroke = 18.dp.toPx()
            val rect = Size(size.width - stroke, size.height - stroke)
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
                drawText(t, topLeft = Offset(size.width / 2 - t.size.width / 2, size.height / 2 - t.size.height / 2))
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
