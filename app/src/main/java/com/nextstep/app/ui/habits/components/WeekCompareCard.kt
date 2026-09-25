package com.nextstep.app.ui.habits.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.nextstep.app.domain.time.DateUtils
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.ui.components.card.LabeledProgress

/** 지난 7일과 그 전 7일. 비교 대상은 나 자신뿐입니다. */
@Composable
internal fun WeekCompareCard(thisWeek: Int, lastWeek: Int) {
    val max = maxOf(thisWeek, lastWeek, 1)
    AppCard {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("지난주와 비교", style = MaterialTheme.typography.titleSmall)
            LabeledProgress("지난 7일", thisWeek.toFloat() / max, MaterialTheme.colorScheme.primary, trailing = DateUtils.formatMinutes(thisWeek))
            LabeledProgress("그 전 7일", lastWeek.toFloat() / max, MaterialTheme.colorScheme.outline, trailing = DateUtils.formatMinutes(lastWeek))
        }
    }
}
