package com.nextstep.app.ui.components.dialog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.selfdirection.SelfDirection
import com.nextstep.app.domain.selfdirection.SelfDirectionStage

/**
 * 이번 주 계획 쓰기: 목표 3개까지, 분(시간)을 계획하는 단계면 시간 칩. [hint] 는 지난주 돌아보기의 "바꿀 것"으로,
 * 계획 → 돌아보기 → 다음 계획이 이어지게 보여 줍니다.
 */
@Composable
fun WeekPlanDialog(
    stage: SelfDirectionStage, forChild: Boolean, goals: List<String>, minutes: Int, hint: String?,
    onDismiss: () -> Unit, onSave: (List<String>, Int) -> Unit,
) {
    val fields = remember { mutableStateListOf<String>().apply { addAll((goals + List(SelfDirection.MAX_GOALS) { "" }).take(SelfDirection.MAX_GOALS)) } }
    var chosen by remember { mutableIntStateOf(minutes) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (forChild) "나의 이번 주 계획" else "이번 주 목표") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (forChild) "이번 주에 해낼 것을 3개까지 적어요. 작고 구체적일수록 좋아요." else "아이가 알아듣기 쉽게 짧고 구체적으로 적어요(예: 그림책 3권).",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                hint?.takeIf { it.isNotBlank() }?.let { Text("지난주에 바꾸기로 한 것: $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                fields.forEachIndexed { i, value ->
                    OutlinedTextField(value = value, onValueChange = { fields[i] = it }, label = { Text("목표 ${i + 1}") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                if (stage.plansMinutes) {
                    Text("이번 주 공부 시간", style = MaterialTheme.typography.labelMedium)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SelfDirection.MINUTE_CHOICES.forEach { m ->
                            FilterChip(selected = chosen == m, onClick = { chosen = if (chosen == m) 0 else m }, label = { Text("${m / MINUTES_PER_HOUR}시간") })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(fields.toList(), if (stage.plansMinutes) chosen else 0) }, enabled = fields.any { it.isNotBlank() } || chosen > 0) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

private const val MINUTES_PER_HOUR = 60
