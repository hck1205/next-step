package com.nextstep.app.ui.growthalbum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstep.app.domain.album.AlbumTalk
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.theme.handStyle

/** "나눈 이야기": 주말 이야기에서 자랑한 것을 인용 카드로(손글씨), 아래에 그 주 · 해 보고 싶었던 것. 최근 것부터 [MAX] 개. */
@Composable
internal fun TalkQuotes(talks: List<AlbumTalk>) {
    val cs = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        talks.takeLast(MAX).reversed().forEach { t ->
            val shape = RoundedCornerShape(18.dp)
            Row(Modifier.fillMaxWidth().background(cs.surface, shape).border(1.dp, cs.outlineVariant, shape).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.FormatQuote, contentDescription = null, tint = cs.primary, modifier = Modifier.size(22.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(t.proud.ifBlank { t.wish }, style = handStyle(27.sp), color = cs.onSurface)
                    val more = listOfNotNull("${DateUtils.formatShortDate(t.week)} 주", t.wish.takeIf { it.isNotBlank() && t.proud.isNotBlank() }?.let { "해 보고 싶은 것: $it" })
                    Text(more.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
            }
        }
    }
}

private const val MAX = 6
