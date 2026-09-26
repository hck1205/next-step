package com.nextstep.app.ui.rewards.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.gamify.BadgeProgress
import com.nextstep.app.ui.components.card.AppCard

/** 배지판: 받은 배지가 먼저, 그다음 가까운 것부터. 아직인 배지는 받는 방법과 지금까지 한 만큼을 보여 줍니다. */
@Composable
internal fun BadgeGrid(badges: List<BadgeProgress>, showsNumbers: Boolean) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            badges.sortedWith(compareByDescending<BadgeProgress> { it.earned }.thenByDescending { it.ratio }).chunked(COLUMNS).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { b -> BadgeCell(b, showsNumbers, Modifier.weight(1f)) }
                    repeat(COLUMNS - row.size) { Column(Modifier.weight(1f)) {} }
                }
            }
        }
    }
}

@Composable
private fun BadgeCell(b: BadgeProgress, showsNumbers: Boolean, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Icon(
            if (b.earned) Icons.Default.MilitaryTech else Icons.Default.Lock, contentDescription = if (b.earned) "받음" else "아직",
            tint = if (b.earned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(28.dp),
        )
        Text(b.badge.label, style = MaterialTheme.typography.labelMedium, color = if (b.earned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
        if (!b.earned) {
            Text(b.badge.how, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            if (showsNumbers) Text("${b.value.coerceAtMost(b.badge.target)}/${b.badge.target}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            val bar = MaterialTheme.colorScheme.secondary
            LinearProgressIndicator(progress = { b.ratio }, modifier = Modifier.fillMaxWidth().height(4.dp), color = bar, trackColor = bar.copy(alpha = TRACK_ALPHA))
        }
    }
}

private const val COLUMNS = 3
private const val TRACK_ALPHA = 0.15f
