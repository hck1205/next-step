package com.nextstep.app.ui.projectcatalog.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.project.ProjectPlan
import com.nextstep.app.domain.project.ProjectPlanner
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/**
 * 시작 단계 고르기. 나이에 맞는 단계가 미리 골라져 있고("추천"), 이미 할 수 있으면 더 뒤 단계를 고릅니다.
 * 고른 단계에 따라 오늘부터 이어 붙인 일정의 도착 예정 달을 바로 보여 줍니다.
 */
@Composable
internal fun StartProjectDialog(plan: ProjectPlan, suggestedIndex: Int, today: LocalDate, onDismiss: () -> Unit, onStart: (Int) -> Unit) {
    var chosen by remember(plan.id) { mutableIntStateOf(suggestedIndex) }
    val finish = remember(plan.id, chosen) { ProjectPlanner.schedule(plan, chosen, today).lastOrNull()?.end }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(plan.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("어느 단계부터 시작할까요?", style = MaterialTheme.typography.bodyMedium)
                Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                    plan.phases.forEachIndexed { i, phase ->
                        Row(
                            Modifier.selectable(selected = chosen == i, onClick = { chosen = i }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = chosen == i, onClick = { chosen = i })
                            Column {
                                Text("${i + 1}. ${phase.title}" + if (i == suggestedIndex) " · 추천" else "", style = MaterialTheme.typography.bodyMedium)
                                Text("${phase.ageLabel} · 하루 약 ${phase.dailyMinutes}분", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                finish?.let { Text("이대로면 ${DateUtils.formatMonth(it)}에 목표에 닿아요", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
            }
        },
        confirmButton = { TextButton(onClick = { onStart(chosen) }) { Text("시작") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
