package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 공부 달력의 진하기 범례: "적음 ▢▢▢▢▢ 많음". 색은 [ChartPalette.heat]. */
@Composable
fun HeatLegend(modifier: Modifier = Modifier) {
    val palette = ChartPalette.current()
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("적음", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        palette.heat.forEach { c -> Box(Modifier.size(width = 12.dp, height = 10.dp).background(c, RoundedCornerShape(3.dp))) }
        Text("많음", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
