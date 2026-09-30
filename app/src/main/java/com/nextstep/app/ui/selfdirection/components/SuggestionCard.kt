package com.nextstep.app.ui.selfdirection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.selfdirection.StageSuggestion
import com.nextstep.app.ui.components.card.AppCard

/** 최근 4주 흔적에서 나온 제안. 학부모만 받아들일 수 있고([onAccept]), 학생에게는 응원 문구로 보입니다. */
@Composable
internal fun SuggestionCard(s: StageSuggestion, onAccept: (() -> Unit)?) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (s.up) "한 칸 맡길 준비가 됐어요" else "잠깐 같이 해 볼까요", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(s.reason, style = MaterialTheme.typography.bodySmall)
            when {
                onAccept == null -> Text(if (s.up) "조금 더 스스로 해 볼 수 있어요. 어른과 이야기해 봐요." else "어려울 땐 같이 계획해도 괜찮아요.", style = MaterialTheme.typography.labelMedium)
                s.up -> Button(onClick = onAccept) { Text("\"${s.to.label}\"로 한 칸 맡기기") }
                else -> OutlinedButton(onClick = onAccept) { Text("\"${s.to.label}\"로 잠깐 같이 하기") }
            }
        }
    }
}
