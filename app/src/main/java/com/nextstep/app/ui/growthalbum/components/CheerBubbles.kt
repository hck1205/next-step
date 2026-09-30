package com.nextstep.app.ui.growthalbum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.album.AlbumCheer
import com.nextstep.app.domain.time.DateUtils

/** "받은 응원": 주고받는 말풍선처럼 번갈아 좌우로(누가 · 날 · 어떤 일에 어떤 응원). 최근 것부터 [MAX] 개. */
@Composable
internal fun CheerBubbles(cheers: List<AlbumCheer>) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        cheers.takeLast(MAX).reversed().forEachIndexed { i, c ->
            val left = i % 2 == 0
            val shape = if (left) RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp) else RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
            Column(
                Modifier.align(if (left) Alignment.Start else Alignment.End).widthIn(max = 300.dp)
                    .background(if (left) cs.primaryContainer else cs.secondaryContainer, shape).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text("${c.fromName} · ${DateUtils.formatShortDate(c.on)}", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                Text("\"${c.taskTitle}\"에 ${c.kind.emoji} ${c.kind.word}", style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
            }
        }
    }
}

private const val MAX = 6
