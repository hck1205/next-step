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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.ui.components.icon.concernIcon

/**
 * 오늘 화면 "전체"의 관심사 머리: 아이콘 · 이름, 카드가 여럿이면 "모두 보기"(그 관심사만 펼침).
 * [label] 로 이름을 바꿀 수 있습니다("먼저 볼 것"). 카드가 하나뿐인 묶음은 머리 한 줄에 카드 이름([subtitle])과 펼치기 버튼([onExpand])까지 담아, 제목 줄이 두 번 쌓이지 않게 합니다.
 */
@Composable
fun GroupHeader(concern: Concern, count: Int, onSeeAll: (() -> Unit)?, subtitle: String? = null, onExpand: (() -> Unit)? = null, label: String = concern.label) {
    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(concernIcon(concern), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(label, style = MaterialTheme.typography.titleMedium)
        Text(
            subtitle?.let { "· $it" }.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
        )
        if (onSeeAll != null && count > 1) TextButton(onClick = onSeeAll) { Text("$count · 모두 보기") }
        if (onExpand != null) {
            IconButton(onClick = onExpand, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.OpenInFull, contentDescription = "${subtitle ?: concern.label} 크게 보기", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
