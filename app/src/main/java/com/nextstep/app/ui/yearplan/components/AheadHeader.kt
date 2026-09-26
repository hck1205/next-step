package com.nextstep.app.ui.yearplan.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** 앞서 가기 묶음의 머리: 구분선 · 이름(학령 전은 "더 해 보면 좋은 것") · 여유가 있을 때만이라는 한 줄. */
@Composable
internal fun AheadHeader(heading: String, note: String) {
    Column(Modifier.padding(top = 12.dp, bottom = 4.dp)) {
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp).padding(end = 4.dp))
            Text(heading, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.tertiary)
        }
        Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
