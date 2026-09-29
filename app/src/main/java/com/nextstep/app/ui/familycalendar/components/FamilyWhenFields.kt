package com.nextstep.app.ui.familycalendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.entry.FamilyEventDraft
import com.nextstep.app.domain.familycalendar.FamilyRepeat
import com.nextstep.app.ui.components.input.ChipRow
import com.nextstep.app.ui.components.input.DateField
import com.nextstep.app.ui.components.input.TimeField

/** 언제: 하루 종일 · 시작·끝 날(여러 날) · 시각 · 반복(과 끝나는 날). */
@Composable
internal fun FamilyWhenFields(draft: FamilyEventDraft, onChange: (FamilyEventDraft) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("언제", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SwitchRow("하루 종일", draft.allDay) { onChange(draft.copy(allDay = it)) }
        DateField("시작", draft.startDate, onChange = { onChange(draft.withStartDate(it)) })
        DateField("끝", draft.endDate, onChange = { onChange(draft.copy(endDate = it)) })
        if (!draft.allDay) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimeField("시작", draft.start, onChange = { onChange(draft.copy(start = it)) }, modifier = Modifier.weight(1f))
                TimeField("끝", draft.end, onChange = { onChange(draft.copy(end = it)) }, modifier = Modifier.weight(1f))
            }
        }
        ChipRow(FamilyRepeat.entries, selected = { it == draft.repeat }, label = { it.label }, onClick = { onChange(draft.copy(repeat = it)) }, title = "반복")
        if (draft.repeat != FamilyRepeat.NONE) {
            val until = draft.repeatUntil
            SwitchRow("끝나는 날 정하기", until != null) { on -> onChange(draft.copy(repeatUntil = if (on) draft.startDate.plusMonths(UNTIL_MONTHS) else null)) }
            if (until != null) DateField("반복 끝", until, onChange = { onChange(draft.copy(repeatUntil = it)) })
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

/** 끝나는 날을 켤 때의 기본값: 시작에서 석 달 뒤. */
private const val UNTIL_MONTHS = 3L
