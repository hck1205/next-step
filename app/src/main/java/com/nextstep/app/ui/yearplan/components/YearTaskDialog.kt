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

/** 올해 할 일 자세히: 언제·어떻게, 이만큼이면 충분(앞서 가기는 도착점과 미리 하면 좋은 까닭), 오늘 할 일로 보내기, 완료. [onSpeak] 가 있으면(어린 단계) 읽어 주기. */
@Composable
internal fun YearTaskDialog(view: YearTaskView, onToggle: () -> Unit, onAddToToday: () -> Unit, onSpeak: ((String) -> Unit)?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(view.task.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${view.task.tier.label} · ${view.task.who.label} · ${view.task.area.label} · ${view.task.term.label}(${view.task.term.months})",
                    style = MaterialTheme.typography.labelMedium, color = if (view.task.isAhead) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                )
                Text(view.task.how, style = MaterialTheme.typography.bodyLarge)
                if (view.task.bar.isNotBlank()) {
                    Text(if (view.task.isAhead) "도착점" else "이만큼이면 충분해요", style = MaterialTheme.typography.labelMedium)
                    Text(view.task.bar, style = MaterialTheme.typography.bodyMedium)
                }
                if (view.task.why.isNotBlank()) {
                    Text("미리 하면", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
                    Text(view.task.why, style = MaterialTheme.typography.bodyMedium)
                }
                if (onSpeak != null) TextButton(onClick = { onSpeak("${view.task.title}. ${view.task.how}") }) { Text("읽어 주기") }
            }
        },
        confirmButton = { TextButton(onClick = { onAddToToday(); onDismiss() }) { Text("오늘 할 일로") } },
        dismissButton = { TextButton(onClick = { onToggle(); onDismiss() }) { Text(if (view.done) "되돌리기" else "다 했어요") } },
    )
}
