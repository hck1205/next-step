package com.nextstep.app.ui.goals.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.GoalStepEntity
import com.nextstep.app.data.model.MilestoneStatus
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.DDayBadge
import com.nextstep.app.ui.components.card.LabeledProgress
import com.nextstep.app.ui.components.card.URGENT_DAYS
import com.nextstep.app.ui.components.row.GoalStepRow
import com.nextstep.app.ui.goals.GoalView
import java.time.LocalDate

/**
 * 날짜가 정해진 목표(시험·수행평가·동아리·수능·수시). 접힌 상태에서는 다음 단계 하나만,
 * 펼치면 날짜가 붙은 전체 단계를 보여 줍니다. 단계는 체크로 끝내고 "할 일로" 보냅니다.
 */
@Composable
fun MissionCard(
    view: GoalView,
    today: LocalDate,
    expanded: Boolean,
    onToggle: () -> Unit,
    canManage: Boolean,
    onSetStepStatus: (GoalStepEntity, MilestoneStatus) -> Unit,
    onSendToTasks: (GoalStepEntity) -> Unit,
    onDelete: () -> Unit,
) {
    val row: @Composable (GoalStepEntity) -> Unit = { step ->
        val due = step.dueDate?.let { LocalDate.ofEpochDay(it) }
        GoalStepRow(
            step = step, goalTitle = null,
            onSetStatus = { onSetStepStatus(step, it) },
            onSendToTasks = if (canManage) ({ onSendToTasks(step) }) else null,
            dueLabel = due?.let { DateUtils.formatDate(it) },
            dueUrgent = due != null && !due.isAfter(today.plusDays(URGENT_DAYS.toLong())),
        )
    }
    AppCard(onClick = onToggle) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    view.kind?.let { Text(it.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    Text(view.goal.title, style = MaterialTheme.typography.titleMedium)
                }
                view.daysLeft?.let { DDayBadge(it) }
            }
            Spacer(Modifier.height(6.dp))
            LabeledProgress(label = "단계 ${view.doneCount}/${view.steps.size}", ratio = view.progress, color = MaterialTheme.colorScheme.primary)
            if (!expanded) {
                view.nextStep?.let { row(it) }
                if (view.overdueSteps > 0) Text("밀린 단계 ${view.overdueSteps}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            } else {
                view.steps.forEach { row(it) }
                if (canManage) TextButton(onClick = onDelete) { Text("삭제", color = MaterialTheme.colorScheme.error) }
            }
        }
    }
}
