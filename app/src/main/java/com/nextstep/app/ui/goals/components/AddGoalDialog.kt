package com.nextstep.app.ui.goals.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.domain.journey.JourneyPeriod
import com.nextstep.app.ui.components.input.OptionPicker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * 직접 만드는 목표. 제목·영역·설명과, 지금 구간부터 [MAX_STEP_PERIODS]개 구간에 하나씩 단계 제목을 받습니다(비워 두면 생략).
 */
@Composable
fun AddGoalDialog(
    periods: List<JourneyPeriod>,
    currentPeriodKey: String?,
    onConfirm: (title: String, area: GoalArea, description: String, stepsByPeriod: List<Pair<String, String>>) -> Unit,
    onDismiss: () -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var area by remember { mutableStateOf(GoalArea.CUSTOM) }
    val start = periods.indexOfFirst { it.key == currentPeriodKey }.coerceAtLeast(0)
    val stepPeriods = remember(periods, currentPeriodKey) { periods.drop(start).take(MAX_STEP_PERIODS) }
    val stepTitles = remember(stepPeriods) { mutableStateListOf<String>().apply { repeat(stepPeriods.size) { add("") } } }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("목표 만들기") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("목표 (예: 초등 졸업 전 피아노 체르니 30)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OptionPicker(options = GoalArea.entries, selected = area, label = { it.label }, onSelect = { area = it })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("왜 이 목표인가요?") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Text("구간별 단계 (비워 두면 생략)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                stepPeriods.forEachIndexed { i, period ->
                    OutlinedTextField(value = stepTitles[i], onValueChange = { stepTitles[i] = it }, label = { Text(period.label) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(title, area, description, stepPeriods.mapIndexed { i, p -> p.key to stepTitles[i] }) }, enabled = title.isNotBlank()) { Text("만들기") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

private const val MAX_STEP_PERIODS = 6
