package com.nextstep.app.ui.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.nextstep.app.domain.gamify.GameStyle
import com.nextstep.app.ui.components.card.AppCard

/**
 * 아이 화면의 게임 요소를 켜고 끄는 스위치. 학부모, 그리고 성장 기록 모양(중등 이후)이면 학생 본인([forStudent]).
 * 지금 나이의 모양([style])을 한 줄로 알려 줍니다. 학생 정보가 아직 없으면 비활성. 꺼도 기록과 약속한 보상은 그대로입니다.
 */
@Composable
internal fun GamifyCard(style: GameStyle, forStudent: Boolean, enabled: Boolean, available: Boolean, onChange: (Boolean) -> Unit) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (forStudent) "${style.title} 보기" else "${style.title.removePrefix("나의 ")} 보여 주기", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    style.summary + " 꺼도 기록과 보상 약속은 그대로예요.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onChange, enabled = available)
        }
    }
}
