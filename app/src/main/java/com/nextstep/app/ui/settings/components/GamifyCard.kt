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
                    when (style) {
                        GameStyle.STICKERS -> "지금 나이에는 한 일마다 스티커 한 장, 10장이면 한 판을 채워요. 숫자·연속 기록은 보이지 않아요."
                        GameStyle.LEVELS -> "해낸 일과 꾸준함으로 레벨이 오르고 배지를 모아요. 점수를 깎거나 남과 비교하지 않아요."
                        GameStyle.GROWTH -> "레벨 이름 대신 누적 기록과 Lv 로 보여요. 연속 기록은 하루 쉬어도 이어지고, 스스로 끌 수도 있어요."
                    } + " 꺼도 기록과 보상 약속은 그대로예요.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onChange, enabled = available)
        }
    }
}
