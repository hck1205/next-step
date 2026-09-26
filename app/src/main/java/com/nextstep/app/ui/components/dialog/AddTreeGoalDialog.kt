package com.nextstep.app.ui.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.nextstep.app.domain.goaltree.GoalTree
import com.nextstep.app.domain.journey.GoalArea
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.OptionPicker
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import java.time.LocalDate

/**
 * 목표 만들기: 무엇을(제목) · 왜(이유) · 분류 · 기한(선택) · 이루면 이어지는 목표(선택).
 * [fixedParent] 가 있으면 "작은 목표 추가"라 이어지는 목표가 정해져 있습니다.
 */
@Composable
fun AddTreeGoalDialog(
    targets: List<GoalEntity>, today: LocalDate, onDismiss: () -> Unit,
    onSave: (title: String, why: String, area: GoalArea, target: LocalDate?, leadsTo: String?) -> Unit,
    fixedParent: GoalEntity? = null, initialArea: GoalArea = GoalArea.CUSTOM,
) {
    var title by remember { mutableStateOf("") }
    var why by remember { mutableStateOf("") }
    var area by remember { mutableStateOf(initialArea) }
    var hasDate by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(today.plusMonths(1)) }
    var parent by remember { mutableStateOf<GoalEntity?>(fixedParent) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (fixedParent != null) "작은 목표 추가" else "목표 만들기") },
        text = {
            Column(Modifier.heightIn(max = DIALOG_MAX.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                fixedParent?.let { Text("이루면 → ${it.title}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
                OutlinedTextField(title, { title = it }, label = { Text("목표(예: 영어 일기 한 달 쓰기)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(why, { why = it }, label = { Text("왜 하나요(선택)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Text("분류", style = MaterialTheme.typography.labelMedium)
                OptionPicker(GoalTree.AREAS, area, label = { it.label }, onSelect = { area = it })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("기한 정하기", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    Switch(checked = hasDate, onCheckedChange = { hasDate = it })
                }
                if (hasDate) DateField("기한", date, onChange = { date = it })
                if (fixedParent == null && targets.isNotEmpty()) {
                    Text("이루면 이어지는 목표(선택)", style = MaterialTheme.typography.labelMedium)
                    OptionPicker(listOf<GoalEntity?>(null) + targets, parent, label = { it?.title ?: "없음" }, onSelect = { parent = it })
                }
            }
        },
        confirmButton = {
            TextButton(enabled = title.isNotBlank(), onClick = { onSave(title, why, area, date.takeIf { hasDate }, parent?.id); onDismiss() }) { Text("만들기") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

private const val DIALOG_MAX = 480
