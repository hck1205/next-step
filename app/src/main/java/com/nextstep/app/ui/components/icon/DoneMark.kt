package com.nextstep.app.ui.components.icon

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** 했는지 표시(채운 체크 · 빈 동그라미). 읽어 주는 말([doneLabel] · [notYetLabel])은 쓰는 곳이 정합니다. */
@Composable
fun DoneMark(done: Boolean, size: Dp, doneLabel: String, notYetLabel: String, modifier: Modifier = Modifier) {
    Icon(
        if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, contentDescription = if (done) doneLabel else notYetLabel,
        tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = modifier.size(size),
    )
}
