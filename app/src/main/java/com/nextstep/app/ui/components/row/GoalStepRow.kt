package com.nextstep.app.ui.components.row

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.MilestoneStatus

/**
 * 목표 단계 한 줄. 체크로 완료, "할 일로" 버튼으로 이번 구간의 할 일에 넣습니다. 정책 없이 콜백만 올립니다.
 * [goalTitle] 이 null 이면 목표 화면 안(목표가 이미 보이는 곳)에서 쓰는 형태입니다.
 */
@Composable
fun GoalStepRow(
    step: GoalStepEntity,
    goalTitle: String?,
    onSetStatus: (MilestoneStatus) -> Unit,
    onSendToTasks: (() -> Unit)?,
    modifier: Modifier = Modifier,
    /** 날짜가 정해진 단계의 마감 표시(예: "4.20"). */
    dueLabel: String? = null,
    dueUrgent: Boolean = false,
) {
    val done = step.status == MilestoneStatus.DONE
    val muted = done || step.status == MilestoneStatus.SKIPPED
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = done, onCheckedChange = { onSetStatus(if (it) MilestoneStatus.DONE else MilestoneStatus.UPCOMING) })
        Column(Modifier.weight(1f)) {
            Text(
                step.title, style = MaterialTheme.typography.bodyMedium,
                textDecoration = if (muted) TextDecoration.LineThrough else null,
                color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
            val sub = listOfNotNull(goalTitle, step.detail.takeIf { it.isNotBlank() }).joinToString(" · ")
            if (sub.isNotBlank()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (dueLabel != null && !muted) Text(dueLabel, style = MaterialTheme.typography.labelSmall, color = if (dueUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
        when {
            step.taskId != null -> Text("할 일에 있음", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            onSendToTasks != null && !muted -> TextButton(onClick = onSendToTasks) { Text("할 일로") }
        }
    }
}
