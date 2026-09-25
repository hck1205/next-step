package com.nextstep.app.ui.yearplan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.ui.yearplan.YearTaskView

/** 올해 할 일 자세히: 언제·어떻게, 오늘 할 일로 보내기, 완료. [onSpeak] 가 있으면(어린 단계) 읽어 주기. */
@Composable
internal fun YearTaskDialog(view: YearTaskView, onToggle: () -> Unit, onAddToToday: () -> Unit, onSpeak: ((String) -> Unit)?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(view.task.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("${view.task.area.label} · ${view.task.term.label}(${view.task.term.months})", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(view.task.how, style = MaterialTheme.typography.bodyLarge)
                if (onSpeak != null) TextButton(onClick = { onSpeak("${view.task.title}. ${view.task.how}") }) { Text("읽어 주기") }
            }
        },
        confirmButton = { TextButton(onClick = { onAddToToday(); onDismiss() }) { Text("오늘 할 일로") } },
        dismissButton = { TextButton(onClick = { onToggle(); onDismiss() }) { Text(if (view.done) "되돌리기" else "다 했어요") } },
    )
}
