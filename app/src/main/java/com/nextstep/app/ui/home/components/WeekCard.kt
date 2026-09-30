package com.nextstep.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.DayMinutes
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard

/**
 * 최근 7일. 어린 단계([showsNumbers] = false)는 공부한 날마다 별 스티커 하나, 숫자 없이.
 * 이후 단계는 불꽃 + 연속 일수 + 이번 주 합계, 요일마다 점(UX 가이드 4-1 "연속 학습").
 */
@Composable
internal fun WeekCard(week: List<DayMinutes>, streak: Int, title: String, showsNumbers: Boolean) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (showsNumbers && streak > 0) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = "연속 학습", tint = MaterialTheme.colorScheme.tertiary)
                    Text("${streak}일", style = MaterialTheme.typography.titleSmall)
                }
                if (showsNumbers) Text(DateUtils.formatMinutes(week.sumOf { it.minutes }), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { day ->
                    val studied = day.minutes > 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            if (studied) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = if (studied) "공부한 날" else "쉰 날",
                            tint = if (studied) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(if (showsNumbers) SMALL_DP.dp else BIG_DP.dp),
                        )
                        Text(DateUtils.dayOfWeekLabel(day.date.dayOfWeek), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

private const val BIG_DP = 34
private const val SMALL_DP = 20
