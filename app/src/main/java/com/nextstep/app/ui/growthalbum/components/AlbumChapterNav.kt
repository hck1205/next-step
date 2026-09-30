package com.nextstep.app.ui.growthalbum.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.growthalbum.AlbumChapter

/** 앨범 차례: 장 이름을 누르면 그 장으로 넘어갑니다. */
@Composable
internal fun AlbumChapterNav(chapters: List<AlbumChapter>, onJump: (AlbumChapter) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        chapters.forEach { c ->
            AssistChip(onClick = { onJump(c) }, label = { Text(c.title) }, leadingIcon = { Icon(c.icon, contentDescription = null, modifier = Modifier.size(16.dp)) })
        }
    }
}
