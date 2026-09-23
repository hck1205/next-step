package com.nextstep.app.ui.journey.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.nextstep.app.ui.components.card.AppCard
import com.nextstep.app.data.local.entity.ActivityEntity

/** 구간에 기록된 활동을 한 줄 칩으로. 눌러 활동 화면으로 갑니다. */
@Composable
internal fun ActivityChips(activities: List<ActivityEntity>, onOpen: () -> Unit) {
    AppCard(onClick = onOpen) {
        Column {
            Text("활동 ${activities.size}개", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary)
            Text(activities.joinToString(" · ") { "${it.type.label} ${it.title}" }, style = MaterialTheme.typography.bodySmall)
        }
    }
}
