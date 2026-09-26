package com.nextstep.app.ui.planhistory.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.goaltree.WeekRate

/** 주별 달성률 막대(8주). 막대 높이 = 그 주 마감 할 일 중 끝낸 비율, 아래에 끝/전체. 이번 주는 진한 색. */
@Composable
internal fun WeekBars(weeks: List<WeekRate>) {
    val scheme = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
        weeks.forEachIndexed { i, w ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(w.rate?.let { "${(it * PERCENT).toInt()}" } ?: "−", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                Box(
                    Modifier.fillMaxWidth().height((BAR_MIN + (BAR_MAX - BAR_MIN) * (w.rate ?: 0f)).dp).clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(if (i == weeks.lastIndex) scheme.primary else scheme.primary.copy(alpha = PAST_ALPHA)),
                )
                Text("${w.weekStart.monthValue}.${w.weekStart.dayOfMonth}", style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                Text("${w.done}/${w.due}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private const val BAR_MIN = 4f
private const val BAR_MAX = 72f
private const val PAST_ALPHA = 0.4f
private const val PERCENT = 100
