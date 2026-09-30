package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.ScoreSeries
import com.nextstep.app.ui.components.card.ColorDot
import com.nextstep.app.ui.theme.subjectColor

/**
 * 과목별 점수 흐름을 두 칸씩 나란히(작은 여러 장). 모든 칸이 같은 눈금([MIN_SCORE]~100점)이라 과목끼리 높이를 견줄 수 있고,
 * 옅은 가로선이 [MID_SCORE]점입니다. 칸 머리에 지금 점수, 아래에 직전 대비를 글자로.
 */
@Composable
fun ScoreMultiples(series: List<ScoreSeries>, modifier: Modifier = Modifier, lineHeight: Dp = 34.dp) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        series.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                pair.forEach { s -> ScoreCell(s, lineHeight, Modifier.weight(1f)) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Text("같은 눈금 $MIN_SCORE~100점 · 가로선 ${MID_SCORE}점", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ScoreCell(s: ScoreSeries, lineHeight: Dp, modifier: Modifier) {
    val palette = ChartPalette.current()
    Column(modifier.semantics(mergeDescendants = true) { contentDescription = "${s.subject.name} 점수 ${s.percents.joinToString(" → ")}" }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ColorDot(subjectColor(s.subject.color), 8)
            Text(s.subject.name, Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${s.last ?: "-"}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        Canvas(Modifier.fillMaxWidth().height(lineHeight)) {
            val y = { v: Int -> size.height - 3.dp.toPx() - (v.coerceIn(MIN_SCORE, MAX_SCORE) - MIN_SCORE).toFloat() / (MAX_SCORE - MIN_SCORE) * (size.height - 6.dp.toPx()) }
            drawLine(palette.grid, Offset(0f, y(MID_SCORE)), Offset(size.width, y(MID_SCORE)), strokeWidth = 1.dp.toPx())
            val n = s.percents.size
            if (n == 1) {
                drawCircle(palette.accent, radius = 4.dp.toPx(), center = Offset(size.width / 2, y(s.percents[0])))
                return@Canvas
            }
            val path = Path().apply {
                s.percents.forEachIndexed { i, v ->
                    val x = i * size.width / (n - 1)
                    if (i == 0) moveTo(x, y(v)) else lineTo(x, y(v))
                }
            }
            drawPath(path, palette.accent, style = Stroke(2.dp.toPx(), join = StrokeJoin.Round))
        }
        Text(s.change?.let { "${if (it >= 0) "+" else ""}${it}점 · 직전 대비" } ?: "시험 1번", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private const val MIN_SCORE = 60
private const val MAX_SCORE = 100
private const val MID_SCORE = 80
