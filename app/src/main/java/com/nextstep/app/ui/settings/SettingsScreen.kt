package com.nextstep.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nextstep.app.data.model.Role
import com.nextstep.app.ui.AppViewModelProvider
import com.nextstep.app.ui.components.AppCard
import com.nextstep.app.ui.components.ConfirmDialog
import com.nextstep.app.ui.components.SectionTitle
import com.nextstep.app.ui.components.SyncStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    var confirmSignOut by remember { mutableStateOf(false) }
    val profile = state.profile

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로") } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("내 정보")
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoRow("역할", profile?.role?.label ?: "-")
                    InfoRow("이름", profile?.displayName ?: "-")
                    InfoRow("학생", profile?.studentName ?: "-")
                }
            }

            SectionTitle("가족 연결")
            AppCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        if (profile?.role == Role.STUDENT) "학부모 앱에서 아래 코드를 입력하면 연결돼요" else "연결된 자녀 코드",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(profile?.pairingCode ?: "------", fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = 6.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { profile?.pairingCode?.let { clipboard.setText(AnnotatedString(it)) } }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "복사")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    SyncStatusBadge(state.syncStatus)
                    if (!state.syncAvailable) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Firebase 가 설정되지 않아 이 기기에만 저장됩니다. app/google-services.json 을 추가하고 다시 빌드하면 학생·학부모 기기 간 실시간 동기화가 켜집니다.",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = viewModel::requestSync) { Text("지금 동기화") }
                    }
                }
            }

            SectionTitle("계정")
            Button(
                onClick = { confirmSignOut = true },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) { Text("이 기기에서 연결 해제") }
            Text(
                "연결 해제하면 이 기기의 역할·가족 정보가 초기화되고 온보딩 화면으로 돌아갑니다. 서버에 동기화된 데이터는 유지됩니다.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (confirmSignOut) {
        ConfirmDialog("연결 해제", "정말 이 기기에서 연결을 해제할까요?", confirmLabel = "해제", onConfirm = viewModel::signOut, onDismiss = { confirmSignOut = false })
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
