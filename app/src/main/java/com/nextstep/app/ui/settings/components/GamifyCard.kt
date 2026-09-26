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
import com.nextstep.app.ui.components.card.AppCard

/** 학부모가 아이 화면의 레벨·배지·이번 주 도전(게임 요소)을 켜고 끄는 스위치. 학생 정보가 아직 없으면 비활성. 약속한 보상은 그대로입니다. */
@Composable
internal fun GamifyCard(enabled: Boolean, available: Boolean, onChange: (Boolean) -> Unit) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("레벨·배지 보여 주기", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    "해낸 일과 꾸준함으로 레벨이 오르고 배지를 받아요. 점수를 깎거나 남과 비교하지 않아요. 꺼도 기록과 보상 약속은 그대로예요.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = onChange, enabled = available)
        }
    }
}
