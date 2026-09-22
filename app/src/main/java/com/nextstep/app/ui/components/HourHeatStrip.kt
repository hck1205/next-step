package com.nextstep.app.ui.components

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

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
