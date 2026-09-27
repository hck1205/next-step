package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 작은 흐름선(지표 타일): 지난 값은 회색 2dp 선, 지금 값만 강조 점(바탕색 테두리). 두 점 이상일 때만 그립니다. */
@Composable
fun Sparkline(values: List<Int>, modifier: Modifier = Modifier, height: Dp = 26.dp) {
    if (values.size < 2) return
    val palette = ChartPalette.current()
    val ring = MaterialTheme.colorScheme.surface
    Canvas(modifier.fillMaxWidth().height(height)) {
        val pad = 5.dp.toPx()
        val lo = values.min()
        val range = (values.max() - lo).coerceAtLeast(1)
        val step = (size.width - pad * 2) / (values.size - 1)
        val pts = values.mapIndexed { i, v -> Offset(pad + i * step, size.height - pad - (v - lo).toFloat() / range * (size.height - pad * 2)) }
        val path = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            pts.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, palette.muted, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(ring, radius = 6.dp.toPx(), center = pts.last())
        drawCircle(palette.accent, radius = 4.dp.toPx(), center = pts.last())
    }
}
