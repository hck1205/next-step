package com.nextstep.app.ui.familycalendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.entry.FamilyEventDraft
import com.nextstep.app.domain.familycalendar.FamilyHeadsUp
import com.nextstep.app.ui.components.input.ChipRow

/** 더 적을 것: 장소 · 준비물 · 오늘 화면에 미리 보이기 · 메모. */
@Composable
internal fun FamilyMoreFields(draft: FamilyEventDraft, onChange: (FamilyEventDraft) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(draft.location, { onChange(draft.copy(location = it)) }, label = { Text("장소 (선택)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(draft.bring, { onChange(draft.copy(bring = it)) }, label = { Text("준비물 (쉼표로 나눠 적어요)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        ChipRow(FamilyHeadsUp.entries, selected = { it == draft.headsUp }, label = { it.label }, onClick = { onChange(draft.copy(headsUp = it)) }, title = "오늘 화면에 미리 보이기")
        OutlinedTextField(draft.memo, { onChange(draft.copy(memo = it)) }, label = { Text("메모 (선택)") }, minLines = 2, modifier = Modifier.fillMaxWidth())
    }
}
