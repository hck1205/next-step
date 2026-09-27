package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 목표 대비 가로 막대(과목별 이번 주 시간, 누가 준 할 일 중 끝낸 것 등). 막대 = 한 것(한 색), 세로 선 = 목표.
 * 값은 줄 끝에 글자로 적어 색만으로 읽지 않게 합니다. 아래 범례의 두 이름은 [doneLabel]·[goalLabel].
 */
@Composable
fun BulletBars(rows: List<BulletRow>, modifier: Modifier = Modifier, doneLabel: String = "한 것", goalLabel: String = "목표") {
    val palette = ChartPalette.current()
    val max = rows.maxOfOrNull { maxOf(it.value, it.goal) }?.coerceAtLeast(1) ?: 1
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { r ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.widthIn(min = NAME_DP.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    r.mark?.let { Box(Modifier.size(8.dp).background(it, RoundedCornerShape(2.dp))) }
                    Text(r.name, style = MaterialTheme.typography.labelMedium)
                }
                BulletTrack(r, max, palette, Modifier.weight(1f))
                Text(r.valueText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("/ ${r.goalText}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        ChartLegend(listOf(LegendKey(doneLabel, palette.accent), LegendKey(goalLabel, palette.ink, line = true)))
    }
}

@Composable
private fun BulletTrack(row: BulletRow, max: Int, palette: ChartPalette, modifier: Modifier) {
    var widthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    Box(
        modifier.height(12.dp).background(palette.track, RoundedCornerShape(4.dp)).onSizeChanged { widthPx = it.width }
            .semantics { contentDescription = "${row.name} ${row.valueText}, 목표 ${row.goalText}" },
    ) {
        if (row.value > 0) Box(Modifier.fillMaxHeight().fillMaxWidth(row.value.toFloat() / max).background(palette.accent, RoundedCornerShape(4.dp)))
        if (row.goal > 0 && widthPx > 0) {
            val x = with(density) { (widthPx * row.goal.toFloat() / max).toDp() - 1.dp }
            Box(Modifier.offset(x = x, y = (-3).dp).width(2.dp).height(18.dp).background(palette.ink, RoundedCornerShape(1.dp)))
        }
    }
}

private const val NAME_DP = 44
