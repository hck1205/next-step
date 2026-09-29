package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.familycalendar.FamilyCalendar
import com.nextstep.app.domain.familytalk.FamilyTalk
import com.nextstep.app.ui.components.card.EmptyCard
import com.nextstep.app.ui.components.row.FamilyEventRow
import com.nextstep.app.ui.familytalk.FamilyTalkUiState

/** 3단계 🎈 다음 주 기대되는 일: 가족 달력의 즐거운 일정 + 해 보고 싶은 것 하나(해야 할 일이 아니라 기대). */
@Composable
internal fun ForwardStep(state: FamilyTalkUiState, wish: String, onWish: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("🎈 다음 주 기대되는 일", style = MaterialTheme.typography.titleMedium)
        if (state.lookForward.isEmpty()) EmptyCard("다음 주 가족 일정이 아직 없어요 · 가족 달력에 즐거운 일을 넣어 봐요")
        state.lookForward.forEach { FamilyEventRow(it, state.members, dayLabel = FamilyCalendar.dayLabel(it.date, state.today)) }
        ChoiceStep("🌱 다음 주에 해 보고 싶은 것", "해야 할 일이 아니라, 해 보고 싶어서 기다려지는 것 하나", FamilyTalk.WISH_IDEAS, wish, onWish)
    }
}
