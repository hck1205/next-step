package com.nextstep.app.ui.components.dialog

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** 창·시트의 "취소" 버튼. 모든 창이 같은 말과 모양을 씁니다. */
@Composable
fun CancelButton(onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text("취소") }
}
