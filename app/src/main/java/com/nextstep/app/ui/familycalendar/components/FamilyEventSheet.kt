package com.nextstep.app.ui.familycalendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.FamilyEventEntity
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.entry.FamilyEventDraft
import com.nextstep.app.domain.familycalendar.FamilyEventKind
import com.nextstep.app.ui.components.dialog.CancelButton
import com.nextstep.app.ui.components.input.ChipRow
import java.time.LocalDate

/**
 * 가족 일정 입력창(+ 시트): 무엇 · 종류 · 언제(하루 종일·여러 날·반복) · 누구의 일정 · 챙기는 사람 · 장소 · 준비물 · 미리 보이기 · 메모.
 * 값 한 벌은 [FamilyEventDraft] 이고 저장 규칙도 거기 있습니다. [onDelete] 가 있으면 고치는 중(반복 일정은 전체가 지워짐).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FamilyEventSheet(
    existing: FamilyEventEntity?,
    date: LocalDate,
    members: List<MemberEntity>,
    onDismiss: () -> Unit,
    onSave: (FamilyEventDraft) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var draft by remember(existing, date) { mutableStateOf(FamilyEventDraft.of(existing, date)) }
    val set: (FamilyEventDraft) -> Unit = { draft = it }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(if (existing == null) "가족 일정 넣기" else "가족 일정 고치기", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(draft.title, { set(draft.copy(title = it)) }, label = { Text("무엇을 하나요") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            ChipRow(FamilyEventKind.entries, selected = { it == draft.kind }, label = { it.label }, onClick = { set(draft.withKind(it, isNew = existing == null)) }, title = "종류")
            FamilyWhenFields(draft, set)
            FamilyWhoFields(draft, members, set)
            FamilyMoreFields(draft, set)
            SheetButtons(
                canSave = draft.canSave,
                onDelete = onDelete?.let { delete -> { delete(); onDismiss() } },
                onSave = { onSave(draft); onDismiss() },
                onCancel = onDismiss,
            )
        }
    }
}

@Composable
private fun SheetButtons(canSave: Boolean, onDelete: (() -> Unit)?, onSave: () -> Unit, onCancel: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (onDelete != null) TextButton(onClick = onDelete) { Text("지우기", color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.weight(1f))
        CancelButton(onCancel)
        Button(onClick = onSave, enabled = canSave) { Text("저장") }
    }
}
