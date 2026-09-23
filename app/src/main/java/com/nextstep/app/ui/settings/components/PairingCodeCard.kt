package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.nextstep.app.data.model.SyncStatus
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SyncStatusBadge

/** 가족 연결 코드와 동기화 상태. 코드 복사, 동기화 요청 버튼. */
@Composable
internal fun PairingCodeCard(code: String?, isStudent: Boolean, syncStatus: SyncStatus, syncAvailable: Boolean, onRequestSync: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    AppCard {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (isStudent) "학부모·멘토 앱에서 아래 코드를 입력하면 연결돼요" else "연결된 학생 코드 · 다른 학부모나 멘토에게 공유할 수 있어요",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(code ?: "------", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = { code?.let { clipboard.setText(AnnotatedString(it)) } }) { Icon(Icons.Default.ContentCopy, contentDescription = "복사") }
            }
            Spacer(Modifier.height(8.dp))
            SyncStatusBadge(syncStatus)
            Spacer(Modifier.height(8.dp))
            if (!syncAvailable) {
                Text(
                    "Firebase 가 설정되지 않아 이 기기에만 저장됩니다. app/google-services.json 을 추가하고 다시 빌드하면 학생·학부모 기기 간 실시간 동기화가 켜집니다.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                OutlinedButton(onClick = onRequestSync) { Text("지금 동기화") }
            }
        }
    }
}
