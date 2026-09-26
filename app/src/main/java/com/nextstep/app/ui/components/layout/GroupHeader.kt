package com.nextstep.app.ui.components.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.hub.Concern
import com.nextstep.app.ui.components.icon.concernIcon

/** 오늘 화면 "전체"의 관심사 머리: 아이콘 · 이름 · 카드 수, 카드가 여럿이면 "모두 보기"(그 관심사 칩을 고름). */
@Composable
fun GroupHeader(concern: Concern, count: Int, onSeeAll: (() -> Unit)?) {
    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(concernIcon(concern), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(concern.label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (onSeeAll != null && count > 1) TextButton(onClick = onSeeAll) { Text("$count · 모두 보기") }
    }
}
