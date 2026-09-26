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
import com.nextstep.app.domain.reward.RewardKind
import com.nextstep.app.domain.reward.RewardTarget
import com.nextstep.app.ui.components.input.OptionPicker
import com.nextstep.app.ui.components.input.SegmentedRow

/**
 * 보상 약속하기(학부모·멘토): 언제(목표를 이루면 / 레벨에 닿으면 / 스티커판을 채우면) · 무엇을.
 * [targets] 는 이 나이에 걸 수 있는 곳만 들어 있고(domain/reward/Rewards.targets), 하나뿐이면(목표 화면) 고르지 않습니다.
 * [ideas] · [hint] 도 나이에 맞춘 예시와 안내입니다.
 */
@Composable
fun PromiseRewardDialog(
    targets: List<RewardTarget>, ideas: List<String>, hint: String, onDismiss: () -> Unit, onSave: (target: RewardTarget, title: String) -> Unit,
    initialTitle: String = "",
) {
    val kinds = targets.map { it.kind }.distinct()
    var kind by remember { mutableStateOf(kinds.firstOrNull() ?: RewardKind.GOAL) }
    var target by remember { mutableStateOf(targets.firstOrNull()) }
    var title by remember { mutableStateOf(initialTitle) }
    val ofKind = targets.filter { it.kind == kind }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialTitle.isNotBlank()) "보상 바꾸기" else "보상 약속하기") },
        text = {
            Column(Modifier.heightIn(max = DIALOG_MAX.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val only = targets.singleOrNull()
                if (only != null) {
                    Text(if (only.kind == RewardKind.GOAL) "\"${only.label}\" 이루면" else only.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                } else {
                    if (kinds.size > 1) {
                        SegmentedRow(kinds, kind, label = { it.label }, onSelect = { k -> kind = k; target = targets.firstOrNull { it.kind == k } }, modifier = Modifier.fillMaxWidth())
                    }
                    target?.takeIf { it.kind == kind }?.let { t -> OptionPicker(ofKind, t, label = { it.label }, onSelect = { target = it }) }
                }
                OutlinedTextField(title, { title = it }, label = { Text("무엇을 해 줄까요") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("예시", style = MaterialTheme.typography.labelMedium)
                ideas.forEach { idea ->
                    Text(idea, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth().clickable { title = idea })
                }
                Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank() && target != null, onClick = { target?.let { onSave(it, title) }; onDismiss() }) { Text("약속하기") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

private const val DIALOG_MAX = 480
