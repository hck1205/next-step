package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.ui.components.icon.concernIcon

/**
 * 오늘 화면 "전체"의 "먼저 볼 것" 머리: 그 카드 관심사의 아이콘 · "먼저 볼 것" · 카드 이름([subtitle]) · 펼치기([onExpand], 자세히 시트).
 */
@Composable
fun FocusHeader(concern: Concern, subtitle: String?, onExpand: () -> Unit) {
    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(concernIcon(concern), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text("먼저 볼 것", style = MaterialTheme.typography.titleMedium)
        Text(
            subtitle?.let { "· $it" }.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onExpand, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.OpenInFull, contentDescription = "${subtitle ?: concern.label} 크게 보기", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
