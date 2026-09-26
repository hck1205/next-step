package com.nextstep.app.ui.components.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.GoalEntity
import com.nextstep.app.domain.reward.RewardKind
import com.nextstep.app.domain.reward.Rewards
import com.nextstep.app.ui.components.input.OptionPicker
import com.nextstep.app.ui.components.input.SegmentedRow

/**
 * 보상 약속하기(학부모·멘토): 언제(목표를 이루면 / 레벨에 닿으면) · 무엇을. 보상은 선택이고 큰 마디에만 겁니다.
 * [fixedGoal] 이 있으면 목표 화면에서 연 것이라 대상이 정해져 있습니다. [levels] 가 비면(게임 요소 꺼짐) 목표만 고릅니다.
 */
@Composable
fun PromiseRewardDialog(
    goals: List<GoalEntity>, levels: List<Int>, onDismiss: () -> Unit, onSave: (kind: RewardKind, targetId: String, title: String) -> Unit,
    fixedGoal: GoalEntity? = null, initialTitle: String = "",
) {
    val kinds = if (fixedGoal != null) listOf(RewardKind.GOAL) else listOfNotNull(RewardKind.GOAL.takeIf { goals.isNotEmpty() }, RewardKind.LEVEL.takeIf { levels.isNotEmpty() })
    var kind by remember { mutableStateOf(kinds.firstOrNull() ?: RewardKind.GOAL) }
    var goal by remember { mutableStateOf(fixedGoal ?: goals.firstOrNull()) }
    var level by remember { mutableStateOf(levels.firstOrNull()) }
    var title by remember { mutableStateOf(initialTitle) }
    val targetId = when (kind) {
        RewardKind.GOAL -> goal?.id
        RewardKind.LEVEL -> level?.toString()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTitle.isNotBlank()) "보상 바꾸기" else "보상 약속하기") },
        text = {
            Column(Modifier.heightIn(max = DIALOG_MAX.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (fixedGoal != null) {
                    Text("\"${fixedGoal.title}\" 이루면", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                } else {
                    if (kinds.size > 1) SegmentedRow(kinds, kind, label = { it.label }, onSelect = { kind = it }, modifier = Modifier.fillMaxWidth())
                    when (kind) {
                        RewardKind.GOAL -> goal?.let { g -> OptionPicker(goals, g, label = { it.title }, onSelect = { goal = it }) }
                        RewardKind.LEVEL -> level?.let { l -> OptionPicker(levels, l, label = { "레벨 $it" }, onSelect = { level = it }) }
                    }
                }
                OutlinedTextField(title, { title = it }, label = { Text("무엇을 해 줄까요") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("예시", style = MaterialTheme.typography.labelMedium)
                Rewards.IDEAS.forEach { idea ->
                    Text(idea, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth().clickable { title = idea })
                }
                Text(
                    "할 일 하나하나가 아니라 큰 마디에만 걸어요. 물건보다 함께하는 시간이 오래 남아요.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank() && targetId != null, onClick = { targetId?.let { onSave(kind, it, title) }; onDismiss() }) { Text("약속하기") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

private const val DIALOG_MAX = 480
