package com.nextstep.app.ui.goal.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
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

/** 이 목표를 이루면 이어지는 목표 고르기. 자기 아래 목표는 목록에 없어 순환이 생기지 않습니다. */
@Composable
internal fun LinkGoalDialog(targets: List<GoalEntity>, current: String?, onDismiss: () -> Unit, onSave: (String?) -> Unit) {
    var chosen by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("이루면 이어지는 목표") },
        text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                (listOf<GoalEntity?>(null) + targets).forEach { g ->
                    Row(Modifier.selectable(selected = chosen == g?.id, onClick = { chosen = g?.id }), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = chosen == g?.id, onClick = { chosen = g?.id })
                        Text(g?.title ?: "없음(맨 위 목표)")
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(chosen); onDismiss() }) { Text("저장") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
