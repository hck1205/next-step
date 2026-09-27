package com.nextstep.app.ui.components.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 한 줄로 쌓은 몫 막대(3칸까지 범주 색). 칸 사이는 2dp 빈틈으로 나누고, 아래 범례에 이름과 몫(%)을 글자로 적습니다.
 */
@Composable
fun ShareBar(parts: List<SharePart>, modifier: Modifier = Modifier) {
    val total = parts.sumOf { it.value }.coerceAtLeast(1)
    Column(modifier) {
        Row(
            Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(4.dp))
                .semantics { contentDescription = parts.joinToString { "${it.label} ${it.value}" } },
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            parts.filter { it.value > 0 }.forEach { p -> Box(Modifier.weight(p.value.toFloat()).fillMaxHeight().background(p.color)) }
        }
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            parts.forEach { p ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.size(10.dp).background(p.color, RoundedCornerShape(3.dp)))
                    Text(p.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${p.value * PERCENT / total}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private const val PERCENT = 100
