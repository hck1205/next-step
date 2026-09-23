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

/** 학부모가 멘토 역할을 겸할지 켜는 스위치. 내 구성원 정보가 아직 없으면 비활성. */
@Composable
internal fun MentorModeCard(enabled: Boolean, available: Boolean, onChange: (Boolean) -> Unit) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("멘토 역할 겸하기", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("직접 자녀를 가르친다면 켜세요. 로드맵 큐레이팅, 과제 배정, 단원·학급 진도 관리가 열립니다.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = enabled, onCheckedChange = onChange, enabled = available)
        }
    }
}
