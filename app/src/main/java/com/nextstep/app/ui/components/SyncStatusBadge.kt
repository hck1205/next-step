package com.nextstep.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.model.SyncStatus

@Composable
fun SyncStatusBadge(status: SyncStatus) {
    val (icon, label, color) = when (status) {
        SyncStatus.LOCAL_ONLY -> Triple(Icons.Default.CloudOff, "로컬 저장", MaterialTheme.colorScheme.onSurfaceVariant)
        SyncStatus.CONNECTING -> Triple(Icons.Default.CloudSync, "연결 중", MaterialTheme.colorScheme.tertiary)
        SyncStatus.SYNCED -> Triple(Icons.Default.CloudDone, "동기화됨", MaterialTheme.colorScheme.secondary)
        SyncStatus.ERROR -> Triple(Icons.Default.CloudOff, "동기화 오류", MaterialTheme.colorScheme.error)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
