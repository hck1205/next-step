package com.nextstep.app.ui.curriculum.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** 지난 학기 · 지금 학기로 · 다음 학기 이동 줄. */
@Composable
internal fun PeriodNavRow(hasPrev: Boolean, hasNext: Boolean, isCurrent: Boolean, canJumpToCurrent: Boolean, onPrev: () -> Unit, onCurrent: () -> Unit, onNext: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onPrev, enabled = hasPrev) { Text("◀ 지난 학기") }
        TextButton(onClick = onCurrent, enabled = !isCurrent && canJumpToCurrent, modifier = Modifier.weight(1f)) { Text(if (isCurrent) "지금 학기" else "지금 학기로") }
        TextButton(onClick = onNext, enabled = hasNext) { Text("다음 학기 ▶") }
    }
}
