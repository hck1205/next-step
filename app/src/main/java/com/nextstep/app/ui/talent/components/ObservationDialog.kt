package com.nextstep.app.ui.talent.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.ObservationEntity
import com.nextstep.app.data.model.AptitudeDomain
import com.nextstep.app.ui.components.input.OptionPicker
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/** 소질 관찰 메모: 영역, 본 장면 한 줄, 강도(1~3). */
@Composable
fun ObservationDialog(today: LocalDate, onConfirm: (ObservationEntity) -> Unit, onDismiss: () -> Unit) {
    var domain by remember { mutableStateOf(AptitudeDomain.MUSIC) }
    var text by remember { mutableStateOf("") }
    var strength by remember { mutableStateOf(2) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("관찰 남기기") },
        text = {
            Column {
                Text("아이가 뭔가를 유난히 잘하거나 오래 붙드는 장면을 봤을 때 적어 두세요. 활동 기록과 합쳐 소질 신호가 됩니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                OptionPicker(options = AptitudeDomain.entries, selected = domain, label = { it.label }, onSelect = { domain = it })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("본 장면 (예: 노래를 한 번 듣고 따라 불렀다)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OptionPicker(options = listOf(1, 2, 3), selected = strength, label = { STRENGTH_LABELS.getValue(it) }, onSelect = { strength = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(ObservationEntity(familyId = "", domain = domain, text = text, strength = strength, date = today.toEpochDay())) }, enabled = text.isNotBlank()) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}

private val STRENGTH_LABELS = mapOf(1 to "그런 것 같다", 2 to "분명히 보인다", 3 to "남들이 먼저 알아본다")
