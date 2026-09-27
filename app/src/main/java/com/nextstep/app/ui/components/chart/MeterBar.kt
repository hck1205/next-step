package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 0~1 몫을 가는 막대로(지표 타일·한눈에 타일). 바탕은 같은 계열의 옅은 색, 채움은 강조색. */
@Composable
fun MeterBar(fraction: Float, modifier: Modifier = Modifier) {
    val palette = ChartPalette.current()
    Box(modifier.fillMaxWidth().height(8.dp).background(palette.track, RoundedCornerShape(4.dp))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).background(palette.accent, RoundedCornerShape(4.dp)))
    }
}
