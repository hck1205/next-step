package com.nextstep.app.ui.goals.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.mission.MissionCatalog
import com.nextstep.app.domain.mission.MissionKind
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.OptionPicker
import java.time.LocalDate

/**
 * 날짜 목표 만들기: 종류 → (필요하면) 과목 → 날짜. 필수 입력은 이 세 가지뿐이고(UX 가이드 3),
 * 세부 단계는 설계대로 자동으로 채워집니다.
 */
@Composable
fun AddMissionDialog(
    kinds: List<MissionKind>,
    subjectNames: List<String>,
    today: LocalDate,
    onConfirm: (MissionKind, LocalDate, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var kind by remember { mutableStateOf(kinds.first()) }
    var subject by remember { mutableStateOf(subjectNames.firstOrNull()) }
    var target by remember(kind) { mutableStateOf(MissionCatalog.suggestedDate(kind, today)) }
    val steps = MissionCatalog.of(kind).steps.size
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("시험·입시 목표") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OptionPicker(options = kinds, selected = kind, label = { it.label }, onSelect = { kind = it })
                if (kind.needsSubject && subjectNames.isNotEmpty()) {
                    OptionPicker(options = subjectNames, selected = subject ?: subjectNames.first(), label = { it }, onSelect = { subject = it })
                }
                DateField(label = if (kind.isExam) "시험일" else "마감일", date = target, onChange = { target = it })
                Text("단계 $steps 개가 날짜에 맞춰 자동으로 생겨요", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(kind, target, subject) }) { Text("만들기") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
