package com.nextstep.app.ui.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextstep.app.domain.familytalk.TalkCard
import com.nextstep.app.domain.familytalk.TalkPhase
import com.nextstep.app.ui.theme.handStyle

/**
 * 오늘 화면의 주말 이야기 카드: 금~일에는 두 빛깔 초대장("같이 이야기하기"), 나눈 뒤에는 "이번 주 기대되는 것" 엽서(해 보고 싶은 것 · 가족 즐거움).
 * 누르면 기록 › 우리 가족 › 주말 이야기로 갑니다.
 */
@Composable
fun FamilyTalkCard(card: TalkCard, onOpen: () -> Unit) {
    when (card.phase) {
        TalkPhase.INVITE -> TalkInvite(onOpen)
        TalkPhase.LOOKING_FORWARD -> PostcardFrame(onClick = onOpen) {
            Text("이번 주 기대되는 것", style = handStyle(32.sp), color = MaterialTheme.colorScheme.onSurface)
            if (card.wish.isNotBlank()) PostcardLine("해 보고 싶은 것", Icons.Filled.Eco, card.wish)
            if (card.treat.isNotBlank()) PostcardLine("가족 즐거움", Icons.Filled.Celebration, card.treat)
            Text("주말 이야기 보기 ›", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        }
        TalkPhase.NONE -> Unit
    }
}

@Composable
private fun TalkInvite(onOpen: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(cs.primaryContainer, cs.tertiaryContainer)), RoundedCornerShape(22.dp))
            .clickable(onClick = onOpen).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = cs.primary, modifier = Modifier.size(16.dp))
            Text("주말 이야기", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = cs.primary)
        }
        Text("이번 주 반짝인 순간을\n모아 두었어요", style = handStyle(34.sp), color = cs.onSurface)
        Text("좋았던 것만 모았어요. 다음 주에 기대되는 것도 같이 골라요.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        Button(onClick = onOpen) { Text("같이 이야기하기") }
    }
}
