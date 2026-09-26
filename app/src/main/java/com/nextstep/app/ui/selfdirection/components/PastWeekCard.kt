package com.nextstep.app.ui.selfdirection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nextstep.app.data.local.entity.WeekPlanEntity
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.icon.moodIcon
import com.nextstep.app.ui.components.icon.moodLabel
import java.time.LocalDate

/** 지난 한 주: 목표(끝낸 것 ✓), 계획 시간, 돌아보기(기분 · 잘된 것 · 어려웠던 것 · 바꿀 것). */
@Composable
internal fun PastWeekCard(p: WeekPlanEntity) {
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${DateUtils.formatDate(LocalDate.ofEpochDay(p.weekStart))} 주", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (p.isReflected) Icon(moodIcon(p.mood), contentDescription = moodLabel(p.mood), tint = MaterialTheme.colorScheme.secondary)
            }
            p.goalList.forEachIndexed { i, g -> Text((if (p.isDone(i)) "✓ " else "· ") + g, style = MaterialTheme.typography.bodySmall) }
            if (p.plannedMinutes > 0) Text("계획 ${p.plannedMinutes}분", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            listOf("잘된 것" to p.good, "어려웠던 것" to p.hard, "바꿀 것" to p.change).filter { it.second.isNotBlank() }.forEach { (l, v) ->
                Text("$l · $v", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
