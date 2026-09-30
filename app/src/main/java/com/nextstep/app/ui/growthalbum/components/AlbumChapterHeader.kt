package com.nextstep.app.ui.growthalbum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstep.app.ui.growthalbum.AlbumChapter
import com.nextstep.app.ui.theme.storyStyle

/** 장의 머리: 빛깔 네모 속 그림 · 장 이름 · 한 줄 요약. */
@Composable
internal fun AlbumChapterHeader(chapter: AlbumChapter, summary: String, modifier: Modifier = Modifier) {
    val (tint, container) = chapterColors(chapter)
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(42.dp).background(container, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Icon(chapter.icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
        Column {
            Text(chapter.title, style = storyStyle(20.sp), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.semantics { heading() })
            if (summary.isNotBlank()) Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 장마다의 빛깔(그림 색, 바탕 색). */
@Composable
internal fun chapterColors(chapter: AlbumChapter): Pair<Color, Color> {
    val cs = MaterialTheme.colorScheme
    return when (chapter) {
        AlbumChapter.GOALS -> cs.tertiary to cs.tertiaryContainer
        AlbumChapter.STEADY, AlbumChapter.TALKS -> cs.primary to cs.primaryContainer
        AlbumChapter.TRIED, AlbumChapter.GREW -> cs.secondary to cs.secondaryContainer
        AlbumChapter.CHEERS -> cs.error to cs.errorContainer
    }
}
