package com.nextstep.app.ui.todo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.data.local.entity.TaskEntity
import com.nextstep.app.data.model.Role
import com.nextstep.app.domain.goaltree.PlanHistory
import com.nextstep.app.domain.taskboard.SubjectLane
import com.nextstep.app.domain.taskboard.TaskSuggestion
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.SubjectTag

/**
 * 과목 한 줄: 머리(과목 · 이번 주 끝낸 수 · 최근 4주 달성률) → 밀린 것 → 오늘 → 다가오는 것([UPCOMING_ROWS]개까지) → 이 과목 추천.
 * 할 일마다 목표 이름과 누가 준 일인지, 추천마다 까닭과 "할 일로 · 목표에 넣기".
 */
@Composable
internal fun LaneCard(
    lane: SubjectLane, goalTitle: (String?) -> String?, goals: List<GoalEntity>, canCheck: Boolean, canAccept: Boolean,
    onToggle: (TaskEntity) -> Unit, onAccept: (TaskSuggestion, String?) -> Unit, onOpenGoal: (String) -> Unit, onOpenSubject: (String) -> Unit,
) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).then(lane.subject?.let { s -> Modifier.clickable { onOpenSubject(s.id) } } ?: Modifier)) {
                    if (lane.subject != null) SubjectTag(lane.subject) else Text("과목 밖 · 목표·생활", style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    listOfNotNull(
                        "이번 주 ${lane.doneThisWeek}개 끝",
                        lane.recentRate?.let { "4주 ${(it * PERCENT).toInt()}%" },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Group("밀린 것", lane.overdue, alert = true, goalTitle, canCheck, onToggle, onOpenGoal)
            Group("오늘", lane.today, alert = false, goalTitle, canCheck, onToggle, onOpenGoal)
            Group("다가오는 것", lane.upcoming.take(UPCOMING_ROWS), alert = false, goalTitle, canCheck, onToggle, onOpenGoal)
            if (lane.upcoming.size > UPCOMING_ROWS) Text("다가오는 할 일 ${lane.upcoming.size - UPCOMING_ROWS}개 더", style = MaterialTheme.typography.labelSmall)
            if (lane.suggestions.isNotEmpty()) {
                HorizontalDivider()
                Text("추천", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                lane.suggestions.forEach { SuggestionRow(it, goals, canAccept, onAccept) }
            }
        }
    }
}

@Composable
private fun Group(
    title: String, tasks: List<TaskEntity>, alert: Boolean, goalTitle: (String?) -> String?, canCheck: Boolean,
    onToggle: (TaskEntity) -> Unit, onOpenGoal: (String) -> Unit,
) {
    if (tasks.isEmpty()) return
    Text("$title ${tasks.size}", style = MaterialTheme.typography.labelMedium, color = if (alert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
    tasks.forEach { t ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = t.done, onCheckedChange = { onToggle(t) }, enabled = canCheck)
            Column(Modifier.weight(1f)) {
                Text(t.title, style = MaterialTheme.typography.bodyMedium)
                val who = Role.from(t.createdByRole)?.takeIf { it != Role.STUDENT }?.let { PlanHistory.assignerLabel(it) + " 준 일" }
                val goal = goalTitle(t.goalId)
                Text(
                    listOfNotNull(t.type.label, DateUtils.formatShortDate(DateUtils.fromEpochDay(t.dueDate)), who, goal?.let { "목표: $it" }).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = t.goalId?.let { id -> Modifier.clickable { onOpenGoal(id) } } ?: Modifier,
                )
            }
        }
    }
}

@Composable
private fun SuggestionRow(s: TaskSuggestion, goals: List<GoalEntity>, canAccept: Boolean, onAccept: (TaskSuggestion, String?) -> Unit) {
    var picking by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(s.title, style = MaterialTheme.typography.bodyMedium)
            Text("${s.source.label} · ${DateUtils.formatShortDate(s.due)}까지", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (canAccept) {
            TextButton(onClick = { onAccept(s, null) }) { Text("할 일로") }
            if (goals.isNotEmpty()) {
                Box {
                    TextButton(onClick = { picking = true }) { Text("목표에") }
                    DropdownMenu(expanded = picking, onDismissRequest = { picking = false }) {
                        goals.forEach { g -> DropdownMenuItem(text = { Text(g.title) }, onClick = { onAccept(s, g.id); picking = false }) }
                    }
                }
            }
        }
    }
}

private const val UPCOMING_ROWS = 5
private const val PERCENT = 100
