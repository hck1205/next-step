package com.nextstep.app.ui.growthalbum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstep.app.data.model.ActivityType
import com.nextstep.app.domain.album.AlbumActivity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.theme.storyStyle

/** "해 본 것": 두 줄 폴라로이드(빛깔 사진 칸에 활동 그림, 아래에 이름 · 날 · 종류). 한 장씩 살짝 기울입니다. */
@Composable
internal fun PolaroidGrid(activities: List<AlbumActivity>) {
    val cs = MaterialTheme.colorScheme
    val tones = listOf(cs.primaryContainer to cs.primary, cs.secondaryContainer to cs.secondary, cs.tertiaryContainer to cs.tertiary)
    Column(Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        activities.chunked(2).forEachIndexed { r, row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                row.forEachIndexed { c, a ->
                    val i = r * 2 + c
                    val shape = RoundedCornerShape(6.dp)
                    Column(
                        Modifier.weight(1f).rotate(if (i % 2 == 0) -2f else 1.6f).shadow(2.dp, shape).background(cs.surface, shape)
                            .border(1.dp, cs.outlineVariant, shape).padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val (bg, fg) = tones[i % tones.size]
                        Box(Modifier.fillMaxWidth().aspectRatio(PHOTO).background(bg, RoundedCornerShape(3.dp)), contentAlignment = Alignment.Center) {
                            Icon(activityIcon(a.type), contentDescription = null, tint = fg, modifier = Modifier.size(34.dp))
                        }
                        Text(a.title, style = storyStyle(16.sp), color = cs.onSurface)
                        Text("${DateUtils.formatShortDate(a.date)} · ${a.type.label}", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun activityIcon(type: ActivityType): ImageVector = when (type) {
    ActivityType.HOBBY -> Icons.Filled.MusicNote
    ActivityType.CLUB -> Icons.Filled.Groups
    ActivityType.FIELD_TRIP -> Icons.Filled.DirectionsBus
    ActivityType.EXPERIENCE -> Icons.Filled.Eco
    ActivityType.VOLUNTEER -> Icons.Filled.Favorite
    ActivityType.COMPETITION -> Icons.Filled.EmojiEvents
    ActivityType.TRAVEL -> Icons.Filled.Map
    ActivityType.OTHER -> Icons.Filled.Star
}

private const val PHOTO = 4f / 3f
