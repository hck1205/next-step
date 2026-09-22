package com.nextstep.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.journey.JourneyItem
import com.nextstep.app.domain.journey.JourneyPhase
import com.nextstep.app.domain.time.DateUtils
import java.time.LocalDate

/**
 * 대시보드용 "지금 준비할 것" 카드. 여정 화면의 요약이며, 항목이 없으면 여정 진입만 보여 줍니다.
 * 정책이 없는 표시 전용 컴포넌트입니다.
 */
@Composable
fun JourneyNowCard(items: List<JourneyItem>, today: LocalDate, hasBirthDate: Boolean, onOpen: () -> Unit, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier, onClick = onOpen) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("성장 여정 · 지금 준비할 것", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        when {
                            !hasBirthDate -> "생년월일을 입력하면 나이대별 준비 항목을 알려 드려요"
                            items.isEmpty() -> "지금 급한 항목은 없어요. 다가오는 일정을 확인해 보세요"
                            else -> "${items.size}개 항목이 준비를 기다려요"
                        },
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                TextButton(onClick = onOpen) { Text("여정") }
            }
            if (items.isNotEmpty()) Spacer(Modifier.height(4.dp))
            items.forEach { item ->
                val overdue = item.phase(today) == JourneyPhase.OVERDUE
                Text(
                    "• ${item.title} · ${DateUtils.formatDate(item.dueDate)}${if (overdue) " 지남" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (overdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
