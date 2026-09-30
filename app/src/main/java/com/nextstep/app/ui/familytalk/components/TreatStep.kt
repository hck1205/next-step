package com.nextstep.app.ui.familytalk.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.familytalk.FamilyTalk
import com.nextstep.app.ui.familytalk.FamilyTalkUiState

/** 4걸음 가족 즐거움: 다음 주에 이미 기다리는 가족 일정이 있으면 입장권으로 먼저 보여 주고, 함께 할 작은 즐거움 하나를 고릅니다. */
@Composable
internal fun TreatStep(state: FamilyTalkUiState, treat: String, onTreat: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.lookForward.firstOrNull()?.let { TalkTicket(it, state.today) }
        ChoiceStep("가족이 함께 할 작은 즐거움", "다음 주를 기다리게 만들 한 가지", Icons.Filled.Celebration, FamilyTalk.TREAT_IDEAS, treat, onTreat)
    }
}
