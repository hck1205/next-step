package com.nextstep.app.ui.habits.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.stats.DayPart
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress

/** 하루 중 언제 공부하는지. 가장 많이 한 때를 진하게. */
@Composable
internal fun DayPartCard(byPart: Map<DayPart, Int>, best: DayPart?) {
    val max = (byPart.values.maxOrNull() ?: 0).coerceAtLeast(1)
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("하루 중 언제", style = MaterialTheme.typography.titleSmall)
            DayPart.entries.forEach { part ->
                val minutes = byPart[part] ?: 0
                LabeledProgress(
                    label = part.label, ratio = minutes.toFloat() / max, trailing = DateUtils.formatMinutes(minutes),
                    color = if (part == best) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}
