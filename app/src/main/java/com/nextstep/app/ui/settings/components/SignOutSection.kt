package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** 이 기기에서 연결 해제 버튼과, 무엇이 지워지고 무엇이 남는지 한 줄. */
@Composable
internal fun SignOutSection(onSignOut: () -> Unit) {
    Button(onClick = onSignOut, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("이 기기에서 연결 해제") }
    Text(
        "연결 해제하면 이 기기의 역할·가족 정보가 초기화되고 온보딩 화면으로 돌아갑니다. 서버에 동기화된 데이터는 유지됩니다.",
        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
