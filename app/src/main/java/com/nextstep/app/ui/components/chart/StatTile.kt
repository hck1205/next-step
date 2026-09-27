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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 지표 타일: 이름 · 값 · 달라진 만큼([delta], 오름·내림 화살표는 좋은 쪽이면 초록 아니면 주황) · 아래 흐름([trend]) 또는 몫([meter]).
 * 색만으로 뜻을 전하지 않도록 변화는 늘 부호 붙은 글자와 화살표로 함께 적고, 글자는 본문 색(대비 확보)입니다.
 */
@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    delta: String? = null,
    up: Boolean? = null,
    good: Boolean? = null,
    trend: (@Composable () -> Unit)? = null,
    meter: Float? = null,
) {
    OutlinedCard(modifier.semantics(mergeDescendants = true) { contentDescription = listOfNotNull(label, value, delta).joinToString(", ") }) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            delta?.let { DeltaLine(it, up, good) }
            Box(Modifier.padding(top = 6.dp)) {
                trend?.invoke()
                meter?.let { Meter(it) }
            }
        }
    }
}

@Composable
private fun DeltaLine(text: String, up: Boolean?, good: Boolean?) {
    val color = when (good) {
        true -> MaterialTheme.colorScheme.secondary
        false -> MaterialTheme.colorScheme.tertiary
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        up?.let { Icon(if (it) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = color, modifier = Modifier.size(14.dp)) }
        Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** 0~1 몫을 가는 막대로. 바탕은 같은 계열의 옅은 색. */
@Composable
private fun Meter(fraction: Float) {
    val palette = ChartPalette.current()
    Box(Modifier.fillMaxWidth().height(8.dp).background(palette.track, RoundedCornerShape(4.dp))) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).background(palette.accent, RoundedCornerShape(4.dp)))
    }
}
