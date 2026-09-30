package com.nextstep.app.ui.familycalendar.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.MemberEntity
import com.nextstep.app.domain.entry.FamilyEventDraft
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.ui.components.input.ChipRow

/** 누구의 일정(여러 명, 아무도 안 고르면 가족 모두) · 챙기는 사람(한 명 또는 없음). */
@Composable
internal fun FamilyWhoFields(draft: FamilyEventDraft, members: List<MemberEntity>, onChange: (FamilyEventDraft) -> Unit) {
    val options = listOf<MemberEntity?>(null) + members
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ChipRow(
            options, title = "누구의 일정",
            selected = { m -> if (m == null) draft.memberIds.isEmpty() else m.id in draft.memberIds },
            label = { it?.name ?: FamilyCalendar.EVERYONE },
            onClick = { m -> onChange(if (m == null) draft.copy(memberIds = emptyList()) else draft.toggleMember(m.id)) },
        )
        ChipRow(
            options, title = "챙기는 사람 (데려다주기 · 예약 · 준비)",
            selected = { it?.id == draft.keeperId },
            label = { it?.name ?: "없음" },
            onClick = { onChange(draft.copy(keeperId = it?.id)) },
        )
    }
}
