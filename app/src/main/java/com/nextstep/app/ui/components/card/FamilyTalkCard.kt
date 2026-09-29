package com.nextstep.app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.familytalk.TalkCard
import com.nextstep.app.domain.familytalk.TalkPhase

/**
 * 오늘 화면의 주말 이야기 카드: 금~일에는 이야기하자는 초대, 나눈 뒤에는 "이번 주 기대되는 것"(해 보고 싶은 것 · 가족 즐거움).
 * 누르면 기록 › 우리 가족 › 주말 이야기로 갑니다.
 */
@Composable
fun FamilyTalkCard(card: TalkCard, onOpen: () -> Unit) {
    AppCard(onClick = onOpen) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            when (card.phase) {
                TalkPhase.INVITE -> {
                    Text("주말 이야기 시간이에요 ✨", style = MaterialTheme.typography.titleMedium)
                    Text("이번 주 반짝인 순간을 모아 두었어요. 다음 주에 기대되는 것도 골라 봐요.", style = MaterialTheme.typography.bodyMedium)
                    FilledTonalButton(onClick = onOpen) { Text("이야기 시작") }
                }
                TalkPhase.LOOKING_FORWARD -> {
                    Text("이번 주 기대되는 것 🎈", style = MaterialTheme.typography.titleMedium)
                    if (card.wish.isNotBlank()) Text("🌱 해 보고 싶은 것 · ${card.wish}", style = MaterialTheme.typography.bodyLarge)
                    if (card.treat.isNotBlank()) Text("🎁 가족 즐거움 · ${card.treat}", style = MaterialTheme.typography.bodyLarge)
                    TextButton(onClick = onOpen) { Text("주말 이야기 보기") }
                }
                TalkPhase.NONE -> Unit
            }
        }
    }
}
