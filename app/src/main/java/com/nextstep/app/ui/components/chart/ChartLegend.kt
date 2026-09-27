package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 차트 아래 범례 한 줄. 이름 글자는 늘 본문 색(계열 색으로 칠하지 않음), 색은 옆 표시가 맡습니다. */
@Composable
fun ChartLegend(keys: List<LegendKey>, modifier: Modifier = Modifier) {
    Row(modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        keys.forEach { k ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    Modifier.size(width = if (k.line) 14.dp else 10.dp, height = if (k.line) 2.dp else 10.dp)
                        .background(k.color, RoundedCornerShape(if (k.line) 1.dp else 3.dp)),
                )
                Text(k.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
