package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 오늘 화면 카드 한 장의 틀: 제목 한 줄과(있으면) 펼치기 버튼 — 누르면 자세히 시트가 열립니다. 그 아래 카드 내용.
 * 카드가 스스로 머리를 가지면 [title] 을 비워 두고, 제목도 펼치기 버튼도 없으면 줄을 그리지 않습니다.
 */
@Composable
fun TodayCardFrame(title: String, onExpand: (() -> Unit)?, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (title.isNotBlank() || onExpand != null) Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            if (onExpand != null) {
                IconButton(onClick = onExpand, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.OpenInFull, contentDescription = "$title 크게 보기", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        content()
    }
}
